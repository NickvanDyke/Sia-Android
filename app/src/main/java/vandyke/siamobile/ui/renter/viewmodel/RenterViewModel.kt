/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import vandyke.siamobile.data.local.File
import vandyke.siamobile.data.local.Node
import vandyke.siamobile.data.remote.SiaError
import vandyke.siamobile.data.siad.SiadService
import vandyke.siamobile.ui.renter.model.RenterModelTest
import vandyke.siamobile.util.siaSubscribe

class RenterViewModel(application: Application) : AndroidViewModel(application) {
    val displayedNodes = MutableLiveData<List<Node>>()
    val path = MutableLiveData<String>()
    val detailsItem = MutableLiveData<Node>()
    val error = MutableLiveData<SiaError>()

    private val model = RenterModelTest()

    private val subscription = SiadService.isSiadLoaded.subscribe {
        if (it)
            refresh()
    }

    init {
        changeDir("")
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    fun refresh() {
        model.refreshDatabase().siaSubscribe({
            changeDir(path.value ?: "")
        }, ::setError)
        // TODO: check that current directory is still valid
    }

    fun changeDir(path: String) {
        println("changing to dir $path")
        model.getImmediateNodes(path).siaSubscribe({ dirs ->
            displayedNodes.value = dirs
            this.path.value = path
        }, ::setError)
    }

    fun goToIndexInPath(index: Int) {
        /* construct the path to go to */
        var path = ""
        val splitPath = this.path.value!!.split("/")
        for (i in 1..index) {
            path += "/${splitPath[i]}"
        }
        changeDir(path)
    }

    fun goUpDir(): Boolean {
        val path = path.value!!
        if (path.isEmpty())
            return false

        val parentPath = path.substring(0, path.lastIndexOf('/'))
        changeDir(parentPath)
        return true
    }

    fun displayDetails(node: Node) {
        detailsItem.value = node
    }

    /**
     * Creates a new directory with the given name in the current directory
     */
    fun createNewDir(name: String) {
        model.createNewDir("${path.value!!}/$name").siaSubscribe({
            refresh()
        }, ::setError)
    }

    fun deleteDir(path: String) {
        model.deleteDir(path).siaSubscribe({
            refresh()
        }, ::setError)
    }

    fun deleteFile(file: File) {
        model.deleteFile(file.path).siaSubscribe({
            refresh()
        }, ::setError)
    }

    private fun setError(err: SiaError) {
        error.value = err
        error.value = null
    }
}