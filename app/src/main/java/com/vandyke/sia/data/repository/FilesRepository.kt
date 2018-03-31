/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.local.daos.*
import com.vandyke.sia.data.models.renter.*
import com.vandyke.sia.data.remote.SiaApi
import com.vandyke.sia.util.diffWith
import com.vandyke.sia.util.replaceLast
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
import java.io.File
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
    fun updateFilesAndDirs(): Completable = api.renterFiles()
            /* get the list of files from the Sia node, delete db files that aren't
             * in the response, and insert each file and the dirs containing it */
            .map { it.files?.sortedBy(SiaFile::path) ?: listOf() }
            .zipWith(db.fileDao().getAllByPath())
            .flatMapObservable { (apiFiles, dbFiles) ->
                val updatedFiles = mutableListOf<SiaFile>()

                apiFiles.diffWith(
                        dbFiles,
                        SiaFile::path,
                        { apiFile, dbFile ->
                            if (apiFile != dbFile) {
                                db.fileDao().insertReplaceOnConflict(apiFile)
                                updatedFiles.add(apiFile)
                            }
                        },
                        {
                            insertFileAndItsParentDirs(it)
                            updatedFiles.add(it)
                        },
                        {
                            db.fileDao().delete(it)
                            updatedFiles.add(it)
                        })

                /* pass the updated files downstream so we can update the dirs containing them */
                updatedFiles.toObservable()
            }
            .flatMap { db.dirDao().getDirsContainingFile(it.path).toElementsObservable() }
            .distinct()
            .flatMapSingle { dir ->
                Single.zip(
                        Single.just(dir),
                        db.fileDao().getFilesUnderDir(dir.path),
                        BiFunction<Dir, List<SiaFile>, Pair<Dir, List<SiaFile>>> { theDir, itsFiles -> theDir to itsFiles })
            }
            /* update the dir */
            .doOnNext { (dir, itsFiles) -> db.dirDao().updateSize(dir.path, itsFiles.sumSize()) }
            .ignoreElements()
            .inDbTransaction(db)

    private fun insertFileAndItsParentDirs(file: SiaFile) {
        db.fileDao().insertAbortOnConflict(file)
        var pathSoFar = ""
        val dirPath = file.parent.split("/")
        dirPath.forEachIndexed { index, pathElement ->
            if (index != 0)
                pathSoFar += "/"
            pathSoFar += pathElement
            db.dirDao().insertIgnoreOnConflict(Dir(pathSoFar, 0))
        }
    }

    fun getDir(path: String) = db.dirDao().getDir(path)

    fun dir(path: String) = db.dirDao().dir(path)

    fun immediateNodes(path: String, orderBy: OrderBy, ascending: Boolean) = Flowable.combineLatest(
            db.dirDao().dirsInDir(path, orderBy = orderBy, ascending = ascending),
            db.fileDao().filesInDir(path, orderBy, ascending),
            BiFunction<List<Dir>, List<SiaFile>, List<Node>> { dir: List<Dir>, file: List<SiaFile> ->
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
                    .flatMapCompletable(::deleteFile))
            .inDbTransaction(db)

    fun moveDir(dir: Dir, newPath: String): Completable {
        /* we move the dir first and then the files under it */
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
             * can't rollback the move in the api as easily as we can roll it back in the db */
            api.renterRename(file.path, newSiapath))
            .inDbTransaction(db)

    /** destination should be a directory */
    fun downloadFile(file: SiaFile, destinationDir: String): Completable = Single.fromCallable {
        var dest = "$destinationDir/${file.name}"
        /* ensure that the file will be downloaded to a location that doesn't already exist
         * by appending a parenthesized number to the name if necessary */
        var destFile = File(dest)
        var i = 1
        while (destFile.exists()) {
            val dotIndex = dest.lastIndexOf('.')
            dest = if (dotIndex == -1) {
                if (i == 1)
                    "$dest ($i)"
                else
                    dest.replaceLast("(${i - 1})", "($i)")
            } else {
                if (i == 1)
                    dest.replaceLast(".", " ($i).")
                else
                    dest.replaceLast("(${i - 1}).", "($i).")
            }
            destFile = File(dest)
            i++
        }
        dest
    }.flatMapCompletable { api.renterDownloadAsync(file.path, it) }

    fun multiDelete(nodes: List<Node>) = nodes.toObservable()
            .flatMapCompletable {
                if (it is Dir)
                    deleteDir(it)
                else
                    deleteFile(it as SiaFile)
            }
            .inDbTransaction(db)

    fun multiDownload(nodes: List<Node>, destinationDir: String): Completable = nodes.toObservable()
            .filter { it is Dir }
            .flatMap { db.fileDao().getFilesUnderDir(it.path).toElementsObservable() }
            .startWith(nodes.filter { it is SiaFile } as List<SiaFile>)
            .distinct(SiaFile::path)
            .flatMapCompletable { downloadFile(it, destinationDir) }

    /* we don't execute this in a db transaction because if early-on moves succeed but later ones
     * fail (due to duplicate paths), we don't want to rollback the early ones, since those files
     * will have already been moved in the api. */
    fun multiMove(nodes: List<Node>, destination: String): Completable = Completable.concatArray(
            nodes.toObservable()
                    .filter { it is Dir }
                    .doOnNext {
                        if (destination.startsWith(it.path) && destination != it.path)
                            throw DirMovedInsideItself(it.name)
                    }
                    .flatMapCompletable {
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