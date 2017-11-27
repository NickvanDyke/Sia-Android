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
import vandyke.siamobile.ui.renter.model.RenterModelTest

class RenterViewModel(application: Application) : AndroidViewModel(application) {
    val root = MutableLiveData<SiaDir>()
    val error = MutableLiveData<SiaError>()

    private val model: IRenterModel = RenterModelTest()

    private val setError: (SiaError) -> Unit = {
        error.value = it
    }

    fun refreshFiles() {
        model.getRootDir(SiaCallback({ it ->
            root.value = it
        }, setError))
    }


}