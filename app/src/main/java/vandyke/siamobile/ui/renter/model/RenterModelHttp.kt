/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.model

import vandyke.siamobile.backend.data.renter.SiaDir
import vandyke.siamobile.backend.networking.Renter
import vandyke.siamobile.backend.networking.SiaCallback

class RenterModelHttp : IRenterModel {
    override fun getRootDir(callback: SiaCallback<SiaDir>) = Renter.files(SiaCallback({ it ->
        val rootDir = SiaDir("root", null)
        it.files.forEach { rootDir + it }
        callback.onSuccess?.invoke(rootDir)
    }, {
        callback.onError(it)
    }))
}