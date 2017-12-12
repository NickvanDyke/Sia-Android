/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.model

import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.data.remote.siaApi

class RenterModelHttp : IRenterModel {

    override fun getRootDir() = siaApi.renterFiles().map {
        val rootDir = SiaDir("rootDir", null)
        it.files.forEach { rootDir.addSiaFile(it) }
        rootDir
    }!!

    override fun createNewDir(path: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}