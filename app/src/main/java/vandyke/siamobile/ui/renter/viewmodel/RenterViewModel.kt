/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.data.remote.SiaError
import vandyke.siamobile.data.remote.subscribeApi
import vandyke.siamobile.ui.renter.model.IRenterModel
import vandyke.siamobile.ui.renter.model.RenterModelTest

class RenterViewModel(application: Application) : AndroidViewModel(application) {
    val rootDir = MutableLiveData<SiaDir>()
    val currentDir = MutableLiveData<SiaDir>()
    val error = MutableLiveData<SiaError>()

    private val model: IRenterModel = RenterModelTest()

    private val setError: (SiaError) -> Unit = {
        error.value = it
    }

    fun refreshFiles() {
        model.getRootDir().subscribeApi({
            rootDir.value = it
            if (currentDir.value == null)
                currentDir.value = it
        }, setError)
    }

    fun goUpDir(): Boolean {
        if (currentDir.value?.parent != null) {
            currentDir.value = currentDir.value!!.parent!!
            return true
        }
        return false
    }
}