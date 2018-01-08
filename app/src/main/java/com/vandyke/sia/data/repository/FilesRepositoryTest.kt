/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.data.models.renter.RenterFilesData
import com.vandyke.sia.db
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.toObservable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.math.BigDecimal

val ROOT_DIR_NAME = "Home"

class FilesRepositoryTest {

    init {
        launch(CommonPool) {
            /* want to always have at least a root directory */
            db.dirDao().insertIgnoreIfConflict(Dir(ROOT_DIR_NAME, BigDecimal.ZERO))
        }
    }

    private val files = mutableListOf(
            RenterFileData("legos/brick/picture.jpg", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/brick/manual", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/brick/blueprint.b", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/brick/draft.txt", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/brick/ad.doc", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/brick/writeup.txt", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/brick/buyers.db", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/brick/listing.html", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/brick/colors.rgb", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/block/picture.jpg", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/block/blueprint", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/block/vector.svg", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/block/colors.rgb", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("legos/blue/brick/picture.jpg", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534),
            RenterFileData("my/name/is/nick/and/this/is/my/story.txt", "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534)
    )

    /* functions that return observables that modify the files list. To (closely) mimic the behavior of using the actual API calls. */
    fun files() = Single.just(RenterFilesData(files))!!

    private fun deleteFileOp(path: String) = Completable.fromCallable {
        var removed: RenterFileData? = null
        files.forEach {
            if (it.path == path) {
                removed = it
                return@forEach
            }
        }
        removed?.let { files.remove(it) }
        // should I be deleting it from the db here too, or wait for the update to do that? Will an immediate update from the node contain the deleted file?
        db.fileDao().deleteFile(path)
    }!!

    private fun addFileOp(siapath: String, source: String, dataPieces: Int, parityPieces: Int) = Completable.fromCallable {
        files.add(RenterFileData(siapath, "eh", BigDecimal("156743"), true, false, 2.0, 663453, 100, 1235534))
    }!!


    /** Queries the list of files from the Sia node, and updates local database from it */
    fun updateFilesAndDirs(): Completable {
        // TODO: also need to remove any files that are in the db but not returned from the API. More efficient way than just deleting all?
        // changing to an observable and then a list is so we have a Single with a list of files, similar to the actual API response
        return files().doOnSuccess {
            /* insert each file to the db and also every dir that leads up to it */
            for (file in it.files) {
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

            /* also add the root dir */
            db.dirDao().insertIgnoreIfConflict(Dir(ROOT_DIR_NAME, BigDecimal.ZERO))

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
                    size += file.filesize
                }
                db.dirDao().insertReplaceIfConflict(Dir(dirAndItsFiles.first.path, size))
            }
        }.toCompletable()!!
    }

    fun immediateNodes(path: String, sortBy: SortBy, ascending: Boolean): Flowable<List<Node>> {
        val dirs: Flowable<List<Dir>>
        val files: Flowable<List<RenterFileData>>
        if (ascending) {
            when (sortBy) {
                SortBy.NAME -> {
                    dirs = db.dirDao().dirsInDirByName(path)
                    files = db.fileDao().filesInDirByName(path)
                }
                SortBy.SIZE -> {
                    dirs = db.dirDao().dirsInDirBySize(path)
                    files = db.fileDao().filesInDirBySize(path)
                }
                SortBy.MODIFIED -> {
                    dirs = db.dirDao().dirsInDirByModified(path)
                    files = db.fileDao().filesInDirByModified(path)
                }
            }
        } else {
            when (sortBy) {
                SortBy.NAME -> {
                    dirs = db.dirDao().dirsInDirByNameDesc(path)
                    files = db.fileDao().filesInDirByNameDesc(path)
                }
                SortBy.SIZE -> {
                    dirs = db.dirDao().dirsInDirBySizeDesc(path)
                    files = db.fileDao().filesInDirBySizeDesc(path)
                }
                SortBy.MODIFIED -> {
                    dirs = db.dirDao().dirsInDirByModifiedDesc(path)
                    files = db.fileDao().filesInDirByModifiedDesc(path)
                }
            }
        }
        return Flowable.combineLatest(
                dirs,
                files,
                BiFunction<List<Dir>, List<RenterFileData>, List<Node>> { dir, file ->
                    return@BiFunction dir + file
                })!!
    }

    fun getDir(path: String) = db.dirDao().getDir(path)

    fun dir(path: String) = db.dirDao().dir(path)

    fun search(name: String, path: String, sortBy: SortBy, ascending: Boolean): Flowable<List<Node>> {
        val dirs: Flowable<List<Dir>>
        val files: Flowable<List<RenterFileData>>
        if (ascending) {
            when (sortBy) {
                SortBy.NAME -> {
                    dirs = db.dirDao().dirsWithNameUnderDirByName(name, path)
                    files = db.fileDao().filesWithNameUnderDirByName(name, path)
                }
                SortBy.SIZE -> {
                    dirs = db.dirDao().dirsWithNameUnderDirBySize(name, path)
                    files = db.fileDao().filesWithNameUnderDirBySize(name, path)
                }
                SortBy.MODIFIED -> {
                    dirs = db.dirDao().dirsWithNameUnderDirByModified(name, path)
                    files = db.fileDao().filesWithNameUnderDirByModified(name, path)
                }
            }
        } else {
            when (sortBy) {
                SortBy.NAME -> {
                    dirs = db.dirDao().dirsWithNameUnderDirByNameDesc(name, path)
                    files = db.fileDao().filesWithNameUnderDirByNameDesc(name, path)
                }
                SortBy.SIZE -> {
                    dirs = db.dirDao().dirsWithNameUnderDirBySizeDesc(name, path)
                    files = db.fileDao().filesWithNameUnderDirBySizeDesc(name, path)
                }
                SortBy.MODIFIED -> {
                    dirs = db.dirDao().dirsWithNameUnderDirByModifiedDesc(name, path)
                    files = db.fileDao().filesWithNameUnderDirByModifiedDesc(name, path)
                }
            }
        }
        return Flowable.combineLatest(
                dirs,
                files,
                BiFunction<List<Dir>, List<RenterFileData>, List<Node>> { dir, file ->
                    return@BiFunction dir + file
                })!!
    }

    fun createDir(path: String) = db.fileDao().getFilesUnder(path).doOnSuccess { filesInNewDir ->
        var size = BigDecimal.ZERO
        filesInNewDir.forEach {
            size += it.filesize
        }
        val newDir = Dir(path, size)
        db.dirDao().insertAbortIfConflict(newDir)
    }.toCompletable()!!

    fun deleteDir(path: String) = db.fileDao().getFilesUnder(path).doOnSuccess { files ->
        files.forEach {
            deleteFile(it.path).subscribe {
                println("deleted file: ${it.path}")
            }
        }
        db.dirDao().deleteDirsUnder(path)
        db.dirDao().deleteDir(path)
        println("deleted dir: $path and the dirs under it")
    }.toCompletable()!!

    fun renameDir(currentName: String, newName: String) {
        // TODO
    }

    fun addFile(siapath: String, source: String, dataPieces: Int, parityPieces: Int) = addFileOp(siapath, source, dataPieces, parityPieces)

    fun deleteFile(path: String) = deleteFileOp(path)

    enum class SortBy {
        NAME,
        SIZE,
        MODIFIED
    }
}