/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.model

import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.data.remote.siaApi

class RenterModelHttp : IRenterModel {
//    override fun getRootDir(callback: SiaCallback<SiaDir>) = Renter.files(SiaCallback({ it ->
//        val rootDir = SiaDir("rootDir", null)
//        it.files.forEach { rootDir + it }
//        callback.onSuccess?.invoke(rootDir)
//    }, {
//        callback.onError(it)
//    }))

    override fun getRootDir() = siaApi.renterFiles().map {
        val rootDir = SiaDir("rootDir", null)
        it.files.forEach { rootDir + it }
        rootDir
    }
}