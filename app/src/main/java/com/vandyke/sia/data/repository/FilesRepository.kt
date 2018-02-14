/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.local.daos.getDirs
import com.vandyke.sia.data.local.daos.getFiles
import com.vandyke.sia.data.local.models.renter.*
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.data.remote.SiaApiInterface
import com.vandyke.sia.util.rx.inDbTransaction
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
            db.dirDao().insertIgnoreOnConflict(Dir("", BigDecimal.ZERO))
        }
    }

    /** Queries the list of files from the Sia node, and updates local database from it */
    fun updateFilesAndDirs(): Completable = Completable.concatArray(
            /* get the list of files from the Sia node, delete db files that aren't
             * in the response, and insert each file and the dirs containing it */
            api.renterFiles()
                    .map { it.files }
                    .zipWith(db.fileDao().getAll())
                    /* remove files that are in the local database but not in the Sia API response */
                    .doOnSuccess { (apiFiles, dbFiles) ->
                        dbFiles.retainAll { it !in apiFiles }
                        dbFiles.forEach {
                            db.fileDao().delete(it.path)
                        }
                    }
                    .flatMapObservable { (apiFiles) -> apiFiles.toObservable() }
                    /* insert each file into the db and also every dir that leads up to it */
                    .doOnNext { file ->
                        db.fileDao().insertReplaceOnConflict(file)
                        /* also add all the dirs leading up to the file */
                        var pathSoFar = ""
                        val dirPath = file.parent.split("/")
                        dirPath.forEachIndexed { index, pathElement ->
                            if (index != 0)
                                pathSoFar += "/"
                            pathSoFar += pathElement
                            db.dirDao().insertIgnoreOnConflict(Dir(pathSoFar, BigDecimal.ZERO))
                        }
                    }
                    .ignoreElements(),
            /* loop through all the dirs containing each file and calculate their values (size, etc.) */
            db.dirDao().getAll()
                    .toElementsObservable()
                    .flatMapSingle { dir ->
                        Single.zip(
                                Single.just(dir),
                                db.fileDao().getFilesUnder(dir.path),
                                BiFunction<Dir, List<RenterFileData>, Pair<Dir, List<RenterFileData>>> { theDir, itsFiles ->
                                    Pair(theDir, itsFiles)
                                })
                    }
                    .doOnNext { (dir, itsFiles) -> db.dirDao().updateSize(dir.path, itsFiles.sumSize()) }
                    .ignoreElements())
            .inDbTransaction(db)


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
                    db.dirDao().insertAbortOnConflict(newDir)
                }
            }
            .onErrorResumeNext {
                Completable.error(if (it is SQLiteConstraintException) DirAlreadyExists(path.name()) else it)
            }!!

    fun deleteDir(path: String) = Completable.concatArray(
            Completable.fromAction { db.dirDao().deleteDir(path) },
            Completable.fromAction { db.dirDao().deleteDirsUnder(path) },
            db.fileDao().getFilesUnder(path)
                    .toElementsObservable()
                    .flatMapCompletable { deleteFile(it) })
            .inDbTransaction(db)

    fun moveDir(dir: Dir, newPath: String) = Completable.concatArray(
            /* we first set the size of the dir and its children to zero, because their size will be updated when the files are moved into them */
            Completable.fromAction { db.dirDao().updateSize(dir.path, BigDecimal.ZERO) },
            db.dirDao().getDirsUnder(dir.path)
                    .toElementsObservable()
                    .flatMapCompletable { childDir ->
                        Completable.fromAction { db.dirDao().updateSize(childDir.path, BigDecimal.ZERO) }
                    },
            Completable.fromAction { db.dirDao().updatePath(dir.path, newPath) }
                    .onErrorResumeNext {
                        Completable.error(if (it is SQLiteConstraintException) DirAlreadyExists(newPath.name()) else it)
                    },
            db.fileDao().getFilesUnder(dir.path)
                    .toElementsObservable()
                    .flatMapCompletable { file -> moveFile(file, file.path.replaceFirst(dir.path, newPath)) })
            .inDbTransaction(db)

    fun downloadDir(dir: Dir) = Completable.fromAction { TODO() }

    fun uploadFile(siapath: String, source: String, dataPieces: Int, parityPieces: Int) = Completable.concatArray(
            Completable.fromAction {
                db.fileDao().insertReplaceOnConflict(RenterFileData(
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
            },
            api.renterUpload(siapath, source, dataPieces, parityPieces))!!

    fun deleteFile(file: RenterFileData) = Completable.concatArray(
            Completable.fromAction { db.fileDao().delete(file.path) },
            db.dirDao().getDirsContainingFile(file.path)
                    .toElementsObservable()
                    .flatMapCompletable { dir ->
                        Completable.fromAction { db.dirDao().updateSize(dir.path, dir.size - file.size) }
                    },
            api.renterDelete(file.path))
            .inDbTransaction(db)

    fun moveFile(file: RenterFileData, newSiapath: String) = Completable.concatArray(
            Completable.fromAction { db.fileDao().updatePath(file.path, newSiapath) }
                    .onErrorResumeNext {
                        Completable.error(if (it is SQLiteConstraintException) FileAlreadyExists(newSiapath.name()) else it)
                    },
            db.dirDao().getDirsContainingFile(file.path)
                    .toElementsObservable()
                    .flatMapCompletable { dir ->
                        Completable.fromAction { db.dirDao().updateSize(dir.path, dir.size - file.size) }
                    },
            db.dirDao().getDirsContainingFile(newSiapath)
                    .toElementsObservable()
                    .flatMapCompletable { dir ->
                        Completable.fromAction { db.dirDao().updateSize(dir.path, dir.size + file.size) }
                    },
            api.renterRename(file.path, newSiapath))
            .inDbTransaction(db)

    fun downloadFile(file: RenterFileData) = Completable.fromAction { TODO() }

    fun multiDelete(nodes: List<Node>) = nodes.toObservable()
            .flatMapCompletable {
                if (it is Dir)
                    deleteDir(it.path)
                else
                    deleteFile(it as RenterFileData)
            }
            .inDbTransaction(db)

    fun multiDownload(nodes: List<Node>) = Completable.fromAction {
        TODO()
    }!!

    /* we don't execute this in a db transaction because if early-on moves succeed but later ones
     * fail (due to duplicate paths), we don't want to rollback the early ones, since those files
     * will have already been moved in the API. */
    fun multiMove(nodes: List<Node>, to: String): Completable = Completable.concatArray(
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
                    })

    enum class OrderBy(val text: String) {
        PATH("path"),
        SIZE("size")
    }

    class DirAlreadyExists(dirName: String) : Throwable("Directory named \"$dirName\" already exists here")
    class FileAlreadyExists(fileName: String) : Throwable("File named \"$fileName\" already exists here")
    class DirMovedInsideItself(dirName: String) : Throwable("Directory \"$dirName\" cannot be moved inside itself")
}