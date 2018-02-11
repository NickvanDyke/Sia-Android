/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.local.daos.getDirs
import com.vandyke.sia.data.local.daos.getFiles
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.data.remote.SiaApiInterface
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

    fun immediateNodes(path: String, orderBy: OrderBy, ascending: Boolean): Flowable<List<Node>> {
        return Flowable.combineLatest(
                db.dirDao().getDirs(path, orderBy = orderBy, ascending = ascending),
                db.fileDao().getFiles(path, orderBy = orderBy, ascending = ascending),
                BiFunction<List<Dir>, List<RenterFileData>, List<Node>> { dir, file ->
                    return@BiFunction dir + file
                })!!
    }

    fun search(name: String, path: String, orderBy: OrderBy, ascending: Boolean): Flowable<List<Node>> {
        return Flowable.combineLatest(
                db.dirDao().getDirs(path, name, orderBy, ascending),
                db.fileDao().getFiles(path, name, orderBy, ascending),
                BiFunction<List<Dir>, List<RenterFileData>, List<Node>> { dir, file ->
                    return@BiFunction dir + file
                })!!
    }

    fun getDir(path: String) = db.dirDao().getDir(path)

    fun dir(path: String) = db.dirDao().dir(path)

    fun createDir(path: String) = db.fileDao().getFilesUnder(path).doOnSuccess { filesInNewDir ->
        var size = BigDecimal.ZERO
        filesInNewDir.forEach {
            size += it.size
        }
        val newDir = Dir(path, size)
        db.dirDao().insertAbortIfConflict(newDir)
    }.toCompletable()!!

    fun deleteDir(path: String) = db.fileDao().getFilesUnder(path).doOnSuccess { files ->
        files.forEach {
            deleteFile(it).subscribe()
        }
        db.dirDao().deleteDirsUnder(path)
        db.dirDao().deleteDir(path)
    }.toCompletable()!!


    fun moveDir(path: String, newPath: String) = Completable.fromAction {
        println("moving dir $path to $newPath")
        db.dirDao().updatePath(path, newPath)
        /* move all the files that were under the dir to the new dir */
        db.fileDao().getFilesUnder(path).subscribe { files ->
            files.forEach { file ->
                println("moving file ${file.path} to ${file.path.replaceFirst(path, newPath)}")
                moveFile(file, file.path.replaceFirst(path, newPath), false).subscribe()
                // TODO: when moveFile updates the dir sizes, it fails to properly subtract it's size
                // from the size of every dir including and after the one that was moved, since it's path has already changed
            }
        }
    }!!

    // not sure if I should be inserting a file here, or just let updating do it
    fun addFile(siapath: String, source: String, dataPieces: Int, parityPieces: Int) = api.renterUpload(siapath, source, dataPieces, parityPieces)
            .doOnComplete {
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
            }!!

    fun deleteFile(file: RenterFileData): Completable = api.renterDelete(file.path).doOnComplete {
        db.fileDao().delete(file.path)
        /* subtract the file's size from the directories that it was previously in */
        db.dirDao().getDirsContainingFile(file.path).subscribe { dirs ->
            dirs.forEach { dir ->
                db.dirDao().updateSize(dir.path, dir.size - file.size)
            }
        }
    }!!

    fun moveFile(file: RenterFileData, newSiapath: String, updateDirSizes: Boolean = true) = api.renterRename(file.path, newSiapath).doOnComplete {
        db.fileDao().updatePath(file.path, newSiapath)
        if (updateDirSizes) {
            /* subtract the file's size from the directories that it was moved out of */
            db.dirDao().getDirsContainingFile(file.path).subscribe { dirs ->
                dirs.forEach { dir ->
                    println("subtracting size of ${file.path} from ${dir.path}")
                    db.dirDao().updateSize(dir.path, dir.size - file.size)
                }
            }
            /* add the file's size to the directories that it was moved into */
            db.dirDao().getDirsContainingFile(newSiapath).subscribe { dirs ->
                dirs.forEach { dir ->
                    println("adding size of $newSiapath to ${dir.path}")
                    db.dirDao().updateSize(dir.path, dir.size + file.size)
                }
            }
        }
    }!!

    enum class OrderBy(val text: String) {
        PATH("path"),
        SIZE("size")
    }
}