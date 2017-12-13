/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.model

import io.reactivex.Completable
import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.data.data.renter.SiaFile
import vandyke.siamobile.data.local.Prefs
import vandyke.siamobile.data.remote.SiaError
import vandyke.siamobile.data.remote.siaApi

class RenterModelHttp : IRenterModel {

    override fun getRootDir() = siaApi.renterFiles().map {
        val rootDir = SiaDir("rootDir", null)
        it.files.forEach { file ->
            rootDir.addSiaNode(file)
            Prefs.renterDirs.add(file.parent.pathStringWithoutRoot)
        }
        Prefs.renterDirs.forEach { path ->
            rootDir.addEmptySiaDirAtPath(path.split("/"))
        }
        rootDir
    }!!

    override fun createNewDir(path: String): Completable {
        return Completable.create {
            if (Prefs.renterDirs.add(path))
                it.onComplete()
            else
                it.onError(SiaError(SiaError.Reason.DIRECTORY_ALREADY_EXISTS))
        }
    }

    override fun deleteDir(dir: SiaDir) {
        Prefs.renterDirs.remove(dir.pathStringWithoutRoot)
        dir.dirs.forEach {
            deleteDir(it)
        }
        dir.files.forEach {
            deleteFile(it)
        }
    }

    override fun addFile(siapath: String, source: String, dataPieces: Int, parityPieces: Int): Completable {
        /* add a local directory for the file's parent directory */
//        Prefs.renterDirs.add(siapath.substring(0, siapath.lastIndexOf("/"))) Might need this, but probably not
        return siaApi.renterUpload(siapath, source, dataPieces, parityPieces)
    }

    override fun deleteFile(file: SiaFile): Completable {
        return siaApi.renterDelete(file.siapath)
    }
}