/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.local.daos.*
import com.vandyke.sia.data.models.renter.*
import com.vandyke.sia.data.remote.SiaApi
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilesRepository
@Inject constructor(
        private val api: SiaApi,
        private val db: AppDatabase
) {
    init {
        launch(CommonPool) {
            /* want to always have at least a root directory */
            db.dirDao().insertIgnoreOnConflict(Dir("", 0))
        }
    }

    /** Queries the list of files from the Sia node, and updates local database from it */
    fun updateFilesAndDirs(): Completable = Completable.concatArray(
            /* get the list of files from the Sia node, delete db files that aren't
             * in the response, and insert each file and the dirs containing it */
            api.renterFiles()
                    .map { it.files ?: listOf() }
                    .zipWith(db.fileDao().getAll())
                    // TODO: might have to change the way db and api files are brought into sync, similar
                    // to in WalletRepository, since apparently ReplaceOnConflict triggers flowable updates even when
                    // the two items are the exact same
                    /* remove files that are in the local database but not in the Sia API response (by path) */
                    .doOnSuccess { (apiFiles, dbFiles) ->
                        dbFiles.filter { dbFile -> apiFiles.find { apiFile -> apiFile.path == dbFile.path } == null }
                                .forEach { db.fileDao().delete(it) }
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
                            db.dirDao().insertIgnoreOnConflict(Dir(pathSoFar, 0))
                        }
                    }
                    .ignoreElements(),
            /* loop through all the dirs containing each file and calculate their values (size, etc.) */
            db.dirDao().getAll()
                    .toElementsObservable()
                    .flatMapSingle { dir ->
                        Single.zip(
                                Single.just(dir),
                                db.fileDao().getFilesUnderDir(dir.path),
                                BiFunction<Dir, List<SiaFile>, Pair<Dir, List<SiaFile>>> { theDir, itsFiles ->
                                    Pair(theDir, itsFiles)
                                })
                    }
                    .doOnNext { (dir, itsFiles) -> db.dirDao().updateSize(dir.path, itsFiles.sumSize()) }
                    .ignoreElements())
            .inDbTransaction(db)


    fun getDir(path: String) = db.dirDao().getDir(path)

    fun dir(path: String) = db.dirDao().dir(path)

    fun immediateNodes(path: String, orderBy: OrderBy, ascending: Boolean) = Flowable.combineLatest(
            db.dirDao().dirsInDir(path, orderBy = orderBy, ascending = ascending),
            db.fileDao().filesInDir(path, orderBy, ascending),
            BiFunction<List<Dir>, List<SiaFile>, List<Node>> { dir, file ->
                return@BiFunction dir + file
            })!!

    fun search(name: String, path: String, orderBy: OrderBy, ascending: Boolean) = Flowable.combineLatest(
            db.dirDao().dirsUnderDirWithName(path, name, orderBy, ascending),
            db.fileDao().filesUnderDirWithName(path, name, orderBy, ascending),
            BiFunction<List<Dir>, List<SiaFile>, List<Node>> { dir, file ->
                return@BiFunction dir + file
            })!!

    fun createDir(path: String) = db.fileDao().getFilesUnderDir(path)
            .flatMapCompletable { filesInNewDir ->
                Completable.fromAction {
                    var size = 0L
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
            .inDbTransaction(db)

    fun deleteDir(dir: Dir) = Completable.concatArray(
            Completable.fromAction { db.dirDao().delete(dir) },
            Completable.fromAction { db.dirDao().deleteDirsUnder(dir.path) },
            db.fileDao().getFilesUnderDir(dir.path)
                    .toElementsObservable()
                    .flatMapCompletable { deleteFile(it) })
            .inDbTransaction(db)

    fun moveDir(dir: Dir, newPath: String): Completable {
        /* we first move the dir and then the files under it */
        var completable = Completable.concatArray(
                Completable.fromAction { db.dirDao().updatePath(dir.path, newPath) }
                        .onErrorResumeNext {
                            Completable.error(if (it is SQLiteConstraintException) DirAlreadyExists(newPath.name()) else it)
                        },
                db.fileDao().getFilesUnderDir(dir.path)
                        .toElementsObservable()
                        .flatMapCompletable { file -> moveFile(file, file.path.replaceFirst(dir.path, newPath)) })

        /* we first set the size of the dir and its children to zero, because their size will be updated when the files are moved into them.
         * If the before/after path is the same, we don't set it's size to zero first, because files' sizes will be subtracted from it also. */
        if (dir.path != newPath)
            completable = completable.startWith(Completable.mergeArray(
                    Completable.fromAction { db.dirDao().updateSize(dir.path, 0) },
                    db.dirDao().getDirsUnder(dir.path)
                            .toElementsObservable()
                            .flatMapCompletable { childDir -> Completable.fromAction { db.dirDao().updateSize(childDir.path, 0) } }))

        return completable.inDbTransaction(db)
    }

    /** destination should be a directory */
    fun downloadDir(dir: Dir, destination: String) = db.fileDao().getFilesUnderDir(dir.path)
            .toElementsObservable()
            .flatMapCompletable { downloadFile(it, destination) }

    fun uploadFile(siapath: String, source: String, dataPieces: Int, parityPieces: Int) =
            api.renterUpload(siapath, source, dataPieces, parityPieces)

    fun deleteFile(file: SiaFile): Completable = Completable.concatArray(
            Completable.fromAction { db.fileDao().delete(file) },
            db.dirDao().getDirsContainingFile(file.path)
                    .toElementsObservable()
                    .flatMapCompletable { dir ->
                        Completable.fromAction { db.dirDao().updateSize(dir.path, dir.size - file.size) }
                    },
            api.renterDelete(file.path))
            .inDbTransaction(db)

    fun moveFile(file: SiaFile, newSiapath: String) = Completable.concatArray(
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
            /* we move the file in the api last because if we move it first and then db actions fail, we
             * can't rollback the move in the api the same way we can roll it back in the db */
            api.renterRename(file.path, newSiapath))
            .inDbTransaction(db)

    /** destination should be a directory */
    fun downloadFile(file: SiaFile, destination: String): Completable = api.renterDownload(file.path, "$destination/${file.name}")

    fun multiDelete(nodes: List<Node>) = nodes.toObservable()
            .flatMapCompletable {
                if (it is Dir)
                    deleteDir(it)
                else
                    deleteFile(it as SiaFile)
            }
            .inDbTransaction(db)

    // TODO: if a dir is selected and so are dirs/files inside of it, then some things will be downloaded multiple times.
    // could check all nodes and eliminate ones that are within/under any selected dirs, then proceed with downloading the
    // remaining nodes
    fun multiDownload(nodes: List<Node>, destination: String): Completable = nodes.toObservable()
            .flatMapCompletable { node ->
                if (node is Dir)
                    downloadDir(node, destination)
                else
                    downloadFile(node as SiaFile, destination)
            }

    /* we don't execute this in a db transaction because if early-on moves succeed but later ones
     * fail (due to duplicate paths), we don't want to rollback the early ones, since those files
     * will have already been moved in the api. */
    fun multiMove(nodes: List<Node>, destination: String): Completable = Completable.concatArray(
            nodes.toObservable()
                    .filter { it is Dir }
                    .doOnNext {
                        if (destination.indexOf(it.path) == 0)
                            throw DirMovedInsideItself(it.name)
                    }.flatMapCompletable {
                        moveDir(it as Dir, destination.withTrailingSlashIfNotEmpty() + it.name)
                    },
            nodes.toObservable()
                    .filter { it is SiaFile }
                    .flatMapCompletable {
                        moveFile(it as SiaFile, destination.withTrailingSlashIfNotEmpty() + it.name)
                    })

    enum class OrderBy(val text: String) {
        PATH("path"),
        SIZE("size")
    }

    class DirAlreadyExists(dirName: String) : Throwable("Directory named \"$dirName\" already exists here")
    class FileAlreadyExists(fileName: String) : Throwable("File named \"$fileName\" already exists here")
    class DirMovedInsideItself(dirName: String) : Throwable("Directory \"$dirName\" cannot be moved inside itself")
}