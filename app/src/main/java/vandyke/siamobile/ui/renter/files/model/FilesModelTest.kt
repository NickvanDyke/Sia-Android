/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.renter.files.model

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import vandyke.siamobile.data.local.data.renter.Dir
import vandyke.siamobile.data.local.data.renter.File
import vandyke.siamobile.data.local.data.renter.Node
import vandyke.siamobile.data.models.renter.RenterFileData
import vandyke.siamobile.db
import java.math.BigDecimal

class FilesModelTest {
    private val files = mutableListOf(RenterFileData("really/long/file/path/because/testing/file.txt", filesize = BigDecimal("498259")),
            RenterFileData("people/jamison/bro", filesize = BigDecimal("116160000000000000000")),
            RenterFileData("people/nick/life.txt", filesize = BigDecimal("847")),
            RenterFileData("people/jeff/panda.png", filesize = BigDecimal("10567219")),
            RenterFileData("colors/red.png", filesize = BigDecimal("48182")),
            RenterFileData("colors/blue.jpg", filesize = BigDecimal("6949")),
            RenterFileData("colors/purple.pdf", filesize = BigDecimal("79")),
            RenterFileData("colors/bright/orange.rgb", filesize = BigDecimal("23583")))


    /** Queries the list of files from the Sia node, and updates local database from it */
    fun refreshDatabase() = Completable.create {
        /* first, insert all files, so that their data can be used to calculate dir values (such as size) */
        files.forEach {
            val newFile = File(it.siapath)
//            println("inserting file ${newFile.path}")
            db.fileDao().insert(newFile)
        }

        files.forEach {
            /* insert directories for each directory in the path to the file */
            var path = ""
            it.siapath.substring(0, it.siapath.lastIndexOf('/')).split("/").forEach {
                path += "/" + it
                val newDir = Dir(path)
//                println("inserting dir ${newDir.path}")
                db.dirDao().insertIgnoreConflict(newDir)
            }
        }
        it.onComplete()
    }!!

    fun getImmediateNodes(path: String) = Flowable.combineLatest(
            db.dirDao().getImmediateDirs(path),
            db.fileDao().getFilesInDir(path),
            BiFunction<List<Dir>, List<File>, List<Node>> { dirs, files ->
                return@BiFunction dirs + files
            })!!


    fun createNewDir(path: String) = Completable.create {
        val newDir = Dir(path)
        db.dirDao().insertAbortIfConflict(newDir)
        it.onComplete()
    }!!

    fun deleteDir(path: String) = Completable.create {
        db.fileDao().getFilesUnder(path).blockingGet().forEach {
            deleteFile(it.path).subscribe()
        }
        db.dirDao().deleteDirsUnder(path)
        db.dirDao().deleteDir(path)
        it.onComplete()
    }!!


    fun addFile(siapath: String, source: String, dataPieces: Int, parityPieces: Int) = Completable.create {
        val newFile = RenterFileData(siapath, filesize = BigDecimal("098123"))
        files.add(newFile)
        it.onComplete()
    }!!

    fun deleteFile(path: String) = Completable.create {
        var removed: RenterFileData? = null
        files.forEach {
            if ("/" + it.siapath == path) {
                removed = it
                return@forEach
            }
        }
        removed?.let { files.remove(it) }
        db.fileDao().deleteFile(path)
        it.onComplete()
    }!!
}