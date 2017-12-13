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
import vandyke.siamobile.data.data.renter.SiaFile
import vandyke.siamobile.data.remote.SiaError
import vandyke.siamobile.data.remote.subscribeApi
import vandyke.siamobile.ui.renter.model.IRenterModel
import vandyke.siamobile.ui.renter.model.RenterModelTest

class RenterViewModel(application: Application) : AndroidViewModel(application) {
    val rootDir = MutableLiveData<SiaDir>()
    val currentDir = MutableLiveData<SiaDir>()
    val error = MutableLiveData<SiaError>()

    private val model: IRenterModel = RenterModelTest()

    fun refreshFiles() {
        model.getRootDir().subscribeApi({
            rootDir.value = it
//            if (currentDir.value == null)
            currentDir.value = it // TODO: determine if currentDir should be changed, if the filepath leading to it has been affected
        }, ::setError)
    }

    fun changeDir(dir: SiaDir) {
        currentDir.value = dir
    }

    fun goUpDir(): Boolean {
        if (currentDir.value?.parent != null) {
            currentDir.value = currentDir.value!!.parent!!
            return true
        }
        return false
    }

    fun createNewDir(name: String) {
        /* passes the full path to the new directory's location, minus the root directory */
        model.createNewDir("${currentDir.value!!.pathStringWithoutRoot}$name").subscribeApi({
            refreshFiles()
        }, ::setError)
    }

    fun deleteDir(dir: SiaDir) {
        model.deleteDir(dir)
        refreshFiles()
    }

    fun deleteFile(file: SiaFile) {
        model.deleteFile(file)
        refreshFiles()
    }

    private fun setError(err: SiaError) {
        error.value = err
        error.value = null
    }
}