/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.data.remote.siaApi
import com.vandyke.sia.db
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal

class RenterRepository {

    /** Queries the list of files from the Sia node, and updates local database from it */
    fun updateFilesAndDirs(): Completable {
        /* first, insert all files, so that their data can be used to calculate dir values (such as size) */
        // should also check for and delete local db files that aren't returned from the sia node


        return siaApi.renterFiles().doAfterSuccess {
            it.files.forEach { file ->
                db.fileDao().insert(file)
                println("inserted file: $file")
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
        }.toCompletable()

//        files.forEach {
//            db.fileDao().insert(it)
//        }
//
//        files.forEach {
//            /* insert directories for each directory in the path to the file */
//            var path = ""
//            it.siapath.substring(0, it.siapath.lastIndexOf('/')).split("/").forEach {
//                path += "/" + it
//                db.fileDao().getFilesUnder(path).subscribe { filesInNewDir ->
//                    var size = BigDecimal.ZERO
//                    filesInNewDir.forEach {
//                        size += it.filesize
//                    }
//                    val newDir = Dir(path, size)
//                    db.dirDao().insertReplaceIfConflict(newDir)
//                }
//            }
//        }
    }

    fun immediateNodes(path: String) = Flowable.combineLatest(
            db.dirDao().dirsInDir(path),
            db.fileDao().filesInDir(path),
            BiFunction<List<Dir>, List<RenterFileData>, List<Node>> { dirs, files ->
                return@BiFunction dirs + files
            })!!


    fun createNewDir(path: String) = Completable.fromCallable {
        val filesInNewDir = db.fileDao().getFilesUnder(path).blockingGet()
        var size = BigDecimal.ZERO
        filesInNewDir.forEach {
            size += it.filesize
        }
        val newDir = Dir(path, size)
        db.dirDao().insertAbortIfConflict(newDir)
    }!!

    // TODO: make more async
    fun deleteDir(path: String) = Completable.create {
        db.fileDao().getFilesUnder(path).blockingGet().forEach {
            //            deleteFile(it.path).subscribe()
        }
        db.dirDao().deleteDirsUnder(path)
        db.dirDao().deleteDir(path)
        it.onComplete()
    }!!


    fun addFile(siapath: String, source: String, dataPieces: Int, parityPieces: Int) = Completable.create {
        it.onComplete()
    }!!

//    fun deleteFile(path: String) = Completable.fromCallable {
//        var removed: RenterFileData? = null
//        files.forEach {
//            if (it.path == path) {
//                removed = it
//                return@forEach
//            }
//        }
//        removed?.let { files.remove(it) }
//        db.fileDao().deleteFile(path)
//    }!!
}