/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import vandyke.siamobile.backend.data.renter.SiaDir
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.ui.renter.model.IRenterModel

class RenterViewModel(application: Application, private val model: IRenterModel) : AndroidViewModel(application) {
    val root = MutableLiveData<SiaDir>()
    val error = MutableLiveData<SiaError>()

    fun refreshFiles() {
        model.getRootDir(SiaCallback({ it ->
            root.value = it
        }, {
            error.value = it
        }))
    }
}