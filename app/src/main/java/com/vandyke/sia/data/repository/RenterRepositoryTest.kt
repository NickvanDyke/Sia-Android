/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.data.models.renter.RenterFilesData
import com.vandyke.sia.db
import com.vandyke.sia.util.emptyIfJustSlash
import com.vandyke.sia.util.slashStart
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.math.BigDecimal

class RenterRepositoryTest {

    init {
        launch(CommonPool) {
            db.dirDao().insertIgnoreIfConflict(Dir("/", BigDecimal.ZERO))
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
        /* first, insert all files, so that their data can be used to calculate dir values (such as size) */
        // should also check for and delete local db files that aren't returned from the sia node

        // changing to an observable and then a list is so we have a Single with a list of files, similar to the actual API response */
        return files().doAfterSuccess {
            it.files.forEach { file ->
                db.fileDao().insert(file)
            }

            // need to update ALL dirs, because filesizes might have changed, and therefore their filesize needs to be recalced
            it.files.forEach {
                /* insert directories for each directory in the path to the file */
                var path = ""
                it.siapath.substring(0, it.siapath.lastIndexOf('/')).split("/").forEach {
                    path += "/" + it
                    db.fileDao().getFilesUnder(path).subscribe { filesInNewDir ->
                        var size = BigDecimal.ZERO
                        filesInNewDir.forEach {
                            size += it.filesize
                        }
                        val newDir = Dir(path, size)
                        db.dirDao().insertReplaceIfConflict(newDir)
                    }
                }
            }

            /* also add the root dir, /  */
            db.fileDao().getFilesUnder("/").subscribe { filesInNewDir ->
                var size = BigDecimal.ZERO
                filesInNewDir.forEach {
                    size += it.filesize
                }
                val newDir = Dir("/", size)
                db.dirDao().insertReplaceIfConflict(newDir)
            }
        }.toCompletable()
    }

    fun immediateNodes(path: String) = Flowable.combineLatest(
            db.dirDao().immediateChildren(path.emptyIfJustSlash()),
            db.fileDao().filesInDir(path.emptyIfJustSlash()),
            BiFunction<List<Dir>, List<RenterFileData>, List<Node>> { dirs, files ->
                return@BiFunction dirs + files
            })!!

    fun getDir(path: String) = db.dirDao().getDir(path.slashStart())

    fun search(name: String, path: String) = db.fileDao().filesWithNameUnderDir(name, path.emptyIfJustSlash())

    fun createNewDir(path: String) = db.fileDao().getFilesUnder(path).doAfterSuccess { filesInNewDir ->
        var size = BigDecimal.ZERO
        filesInNewDir.forEach {
            size += it.filesize
        }
        val newDir = Dir(path, size)
        db.dirDao().insertAbortIfConflict(newDir)
    }.toCompletable()!!

    fun deleteDir(path: String) = db.fileDao().getFilesUnder(path).doAfterSuccess { files ->
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

    }

    fun addFile(siapath: String, source: String, dataPieces: Int, parityPieces: Int) = addFileOp(siapath, source, dataPieces, parityPieces)

    fun deleteFile(path: String) = deleteFileOp(path)
}