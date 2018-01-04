/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.SiaError
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.repository.RenterRepositoryTest
import com.vandyke.sia.isSiadLoaded
import com.vandyke.sia.util.siaSubscribe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class FilesViewModel : ViewModel() {
    val displayedNodes = MutableLiveData<List<Node>>()
    val path = MutableLiveData<String>()
    val detailsItem = MutableLiveData<Node>()
    val error = MutableLiveData<SiaError>()

    private val renterRepo = RenterRepositoryTest()

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
        renterRepo.updateFilesAndDirs().subscribeOn(Schedulers.io()).subscribe()
        // TODO: check that current directory is still valid. and track/display progress of update
    }

    fun changeDir(path: String) {
        currentDirSubscription?.dispose()
        currentDirSubscription = renterRepo.immediateNodes(path).siaSubscribe({ nodes ->
            displayedNodes.value = nodes
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
        renterRepo.createNewDir("${path.value!!}/$name").siaSubscribe(::refresh, ::setError)
    }

    fun deleteDir(path: String) {
        renterRepo.deleteDir(path).siaSubscribe({ println("deleteDir signaled complete"); refresh() }, ::setError)
    }

    fun deleteFile(path: String) {
        renterRepo.deleteFile(path).siaSubscribe(::refresh, ::setError)
    }

    fun addFile(path: String) {
        renterRepo.addFile(path, "blah", 10, 20).siaSubscribe(::refresh, ::setError)
    }

    fun renameFile(currentName: String, newName: String) {

    }

    fun renameDir(currentName: String, newName: String) {

    }

    private fun setError(err: SiaError) {
        error.value = err
        error.value = null
    }
}