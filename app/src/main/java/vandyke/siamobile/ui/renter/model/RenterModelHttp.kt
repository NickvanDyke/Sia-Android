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
        it.files.forEach { rootDir.addSiaFile(it) }
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteFile(file: SiaFile): Completable {
        return siaApi.renterDelete(file.siapath)
    }
}