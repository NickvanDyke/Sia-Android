/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.local.daos.getDirs
import com.vandyke.sia.data.local.daos.getFiles
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.local.models.renter.name
import com.vandyke.sia.data.local.models.renter.withTrailingSlashIfNotEmpty
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.data.remote.SiaApiInterface
import com.vandyke.sia.util.rx.asDbTransaction
import com.vandyke.sia.util.rx.toElementsObservable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.toObservable
import io.reactivex.rxkotlin.zipWith
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilesRepository
@Inject constructor(
        private val api: SiaApiInterface,
        private val db: AppDatabase
) {
    init {
        launch(CommonPool) {
            /* want to always have at least a root directory */
            db.dirDao().insertIgnoreIfConflict(Dir("", BigDecimal.ZERO))
        }
    }

    /** Queries the list of files from the Sia node, and updates local database from it */
    // TODO: Rxify more
    fun updateFilesAndDirs(): Completable {
        return api.renterFiles().zipWith(db.fileDao().getAll()).doOnSuccess { (files, dbFiles) ->
            /* remove files that are in the local database but not in the Sia API response */
            dbFiles.retainAll { it !in files.files }
            dbFiles.forEach {
                db.fileDao().delete(it.path)
            }
            /* insert each file into the db and also every dir that leads up to it */
            for (file in files.files) {
                db.fileDao().insert(file)
                /* also add all the dirs leading up to the file */
                var pathSoFar = ""
                val dirPath = file.parent?.split("/") ?: continue
                dirPath.forEachIndexed { index, pathElement ->
                    if (index != 0)
                        pathSoFar += "/"
                    pathSoFar += pathElement
                    db.dirDao().insertIgnoreIfConflict(Dir(pathSoFar, BigDecimal.ZERO))
                }
            }

            /* loop through all the dirs and calculate their values (filesize etc) */
            db.dirDao().getAll().flatMapObservable { list ->
                list.toObservable()
            }.flatMapSingle { dir ->
                        Single.zip(db.fileDao().getFilesUnder(dir.path),
                                Single.just(dir),
                                BiFunction<List<RenterFileData>, Dir, Pair<Dir, List<RenterFileData>>> { filesUnderDir, dir ->
                                    Pair(dir, filesUnderDir)
                                })
                    }.subscribe { dirAndItsFiles ->
                        var size = BigDecimal.ZERO
                        dirAndItsFiles.second.forEach { file ->
                            size += file.size
                        }
                        db.dirDao().insertReplaceIfConflict(Dir(dirAndItsFiles.first.path, size))
                    }
        }.toCompletable()!!
    }

//    fun updateFilesAndDirs(): Completable = api.renterFiles()
//            .flatMapObservable { it.files.toObservable() }


    fun getDir(path: String) = db.dirDao().getDir(path)

    fun dir(path: String) = db.dirDao().dir(path)

    fun immediateNodes(path: String, orderBy: OrderBy, ascending: Boolean) = Flowable.combineLatest(
            db.dirDao().getDirs(path, orderBy = orderBy, ascending = ascending),
            db.fileDao().getFiles(path, orderBy = orderBy, ascending = ascending),
            BiFunction<List<Dir>, List<RenterFileData>, List<Node>> { dir, file ->
                return@BiFunction dir + file
            })!!

    fun search(name: String, path: String, orderBy: OrderBy, ascending: Boolean) = Flowable.combineLatest(
            db.dirDao().getDirs(path, name, orderBy, ascending),
            db.fileDao().getFiles(path, name, orderBy, ascending),
            BiFunction<List<Dir>, List<RenterFileData>, List<Node>> { dir, file ->
                return@BiFunction dir + file
            })!!

    fun createDir(path: String) = db.fileDao().getFilesUnder(path)
            .flatMapCompletable { filesInNewDir ->
                Completable.fromAction {
                    var size = BigDecimal.ZERO
                    filesInNewDir.forEach {
                        size += it.size
                    }
                    val newDir = Dir(path, size)
                    db.dirDao().insertAbortIfConflict(newDir)
                }
            }
            .onErrorResumeNext {
                Completable.error(if (it is SQLiteConstraintException) DirAlreadyExists(path.name()) else it)
            }!!

    fun deleteDir(path: String) = Completable.concatArray(
            Completable.fromAction { db.dirDao().deleteDir(path) },
            Completable.fromAction { db.dirDao().deleteDirsUnder(path) },
            db.fileDao().getFilesUnder(path).toElementsObservable()
                    .flatMapCompletable { deleteFile(it) }
    ).asDbTransaction(db)

    fun moveDir(dir: Dir, newPath: String) = Completable.concatArray(
            /* we first set the size of the dir to zero, because its size will be updated when its files are moved into it */
            Completable.fromAction { db.dirDao().updateSize(dir.path, BigDecimal.ZERO) },
            Completable.fromAction { db.dirDao().updatePath(dir.path, newPath) }
                    .onErrorResumeNext {
                        Completable.error(if (it is SQLiteConstraintException) DirAlreadyExists(dir.name) else it)
                    },
            db.fileDao().getFilesUnder(dir.path).toElementsObservable()
                    .flatMapCompletable { file -> moveFile(file, file.path.replaceFirst(dir.path, newPath)) }
    ).asDbTransaction(db)

    fun addFile(siapath: String, source: String, dataPieces: Int, parityPieces: Int) = Completable.concatArray(
            api.renterUpload(siapath, source, dataPieces, parityPieces),
            Completable.fromAction {
                db.fileDao().insert(RenterFileData(
                        siapath,
                        source,
                        BigDecimal.ZERO,
                        false,
                        false,
                        0.0,
                        0,
                        0,
                        0
                ))
            }
    )!!

    fun deleteFile(file: RenterFileData) = Completable.concatArray(
            api.renterDelete(file.path),
            Completable.fromAction { db.fileDao().delete(file.path) },
            db.dirDao().getDirsContainingFile(file.path).toElementsObservable()
                    .flatMapCompletable { dir ->
                        Completable.fromAction { db.dirDao().updateSize(dir.path, dir.size - file.size) }
                    }
    ).asDbTransaction(db)

    fun moveFile(file: RenterFileData, newSiapath: String) = Completable.concatArray(
            api.renterRename(file.path, newSiapath),
            Completable.fromAction { db.fileDao().updatePath(file.path, newSiapath) }
                    .onErrorResumeNext {
                        Completable.error(if (it is SQLiteConstraintException) FileAlreadyExists(file.name) else it)
                    },
            db.dirDao().getDirsContainingFile(file.path).toElementsObservable()
                    .flatMapCompletable { dir ->
                        Completable.fromAction { db.dirDao().updateSize(dir.path, dir.size - file.size) }
                    },
            db.dirDao().getDirsContainingFile(newSiapath).toElementsObservable()
                    .flatMapCompletable { dir ->
                        Completable.fromAction { db.dirDao().updateSize(dir.path, dir.size + file.size) }
                    }
    ).asDbTransaction(db)

    fun multiDelete(nodes: List<Node>) = nodes.toObservable()
            .flatMapCompletable {
                if (it is Dir)
                    deleteDir(it.path)
                else
                    deleteFile(it as RenterFileData)
            }
            .asDbTransaction(db)

    fun multiDownload(nodes: List<Node>) = Completable.fromAction {
        TODO()
    }!!

    fun multiMove(nodes: List<Node>, to: String) = Completable.concatArray(
            nodes.toObservable()
                    .filter { it is Dir }
                    .doOnNext {
                        if (to.indexOf(it.path) == 0)
                            throw DirMovedInsideItself(it.name)
                    }.flatMapCompletable {
                        moveDir(it as Dir, to.withTrailingSlashIfNotEmpty() + it.name)
                    },
            nodes.toObservable()
                    .filter { it is RenterFileData }
                    .flatMapCompletable {
                        moveFile(it as RenterFileData, to.withTrailingSlashIfNotEmpty() + it.name)
                    }
    ).asDbTransaction(db)

    enum class OrderBy(val text: String) {
        PATH("path"),
        SIZE("size")
    }

    class DirAlreadyExists(dirName: String) : Throwable("Directory named \"$dirName\" already exists in this location")
    class FileAlreadyExists(fileName: String) : Throwable("File named \"$fileName\" already exists in this location")
    class DirMovedInsideItself(dirName: String) : Throwable("Directory \"$dirName\" cannot be moved inside itself")
}