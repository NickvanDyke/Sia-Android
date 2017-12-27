/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.renter.files.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vandyke.siamobile.data.local.data.renter.File
import vandyke.siamobile.data.local.data.renter.Node
import vandyke.siamobile.data.remote.SiaError
import vandyke.siamobile.isSiadLoaded
import vandyke.siamobile.ui.renter.files.model.FilesModelTest
import vandyke.siamobile.util.siaSubscribe

class FilesViewModel : ViewModel() {
    val displayedNodes = MutableLiveData<List<Node>>()
    val path = MutableLiveData<String>()
    val detailsItem = MutableLiveData<Node>()
    val error = MutableLiveData<SiaError>()

    private val model = FilesModelTest()

    private val subscription = isSiadLoaded.subscribe {
        if (it)
            refresh()
    }

    private var currentDirSubscription: Disposable? = null

    init {
        path.value = ""
        displayedNodes.value = listOf()
        changeDir("")
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    fun refresh() {
        model.refreshDatabase().subscribeOn(Schedulers.io()).subscribe()
        // TODO: check that current directory is still valid
    }

    fun changeDir(path: String) {
        currentDirSubscription?.dispose()
        currentDirSubscription = model.getImmediateNodes(path).siaSubscribe({ dirs ->
            displayedNodes.value = dirs
            this.path.value = path
        }, ::setError)
    }

    fun goToIndexInPath(index: Int) {
        /* construct the path to go to */
        val splitPath = this.path.value!!.split("/")
        var path = ""
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