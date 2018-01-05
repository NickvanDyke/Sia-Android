/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.SiaError
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.repository.RenterRepositoryTest
import com.vandyke.sia.isSiadLoaded
import com.vandyke.sia.util.siaSubscribe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class FilesViewModel : ViewModel() {
    val displayedNodes = MutableLiveData<List<Node>>()
    val currentDir = MutableLiveData<Dir>()
    val searchTerm = MutableLiveData<String>()
    val detailsItem = MutableLiveData<Node>()
    val error = MutableLiveData<SiaError>()

    val currentDirPath
        get() = currentDir.value?.path ?: ""

    private val renterRepo = RenterRepositoryTest()

    private val subscription = isSiadLoaded.subscribe {
        if (it)
            refresh()
    }

    private var nodesSubscription: Disposable? = null
        set(value) {
            field?.dispose()
            field = value
        }

    init {
        displayedNodes.value = listOf()
        changeDir("/")
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
        if (searchTerm.value != null) {
            search(searchTerm.value!!)
        } else {
            nodesSubscription = renterRepo.immediateNodes(path).siaSubscribe({ nodes ->
                displayedNodes.value = nodes
            }, ::onError)
        }


        renterRepo.getDir(path).siaSubscribe({
            currentDir.value = it
        }, ::onError)
    }

    fun goToIndexInPath(index: Int) {
        /* construct the path to go to */
        val splitPath = currentDirPath.split("/")
        var path = ""
        for (i in 1..index) {
            path += "/${splitPath[i]}"
        }
        changeDir(path)
    }

    fun goUpDir(): Boolean {
        if (currentDirPath.isEmpty())
            return false
        changeDir(currentDir.value!!.parent)
        return true
    }

    fun displayDetails(node: Node) {
        detailsItem.value = node
    }

    /**
     * Creates a new directory with the given name in the current directory
     */
    fun createDir(name: String) {
        renterRepo.createNewDir("$currentDirPath/$name").siaSubscribe(::refresh, ::onError)
    }

    fun deleteDir(path: String) {
        renterRepo.deleteDir(path).siaSubscribe({ println("deleteDir signaled complete"); refresh() }, ::onError)
    }

    fun deleteFile(path: String) {
        renterRepo.deleteFile(path).siaSubscribe(::refresh, ::onError)
    }

    fun addFile(path: String) {
        renterRepo.addFile(path, "blah", 10, 20).siaSubscribe(::refresh, ::onError)
    }

    fun renameFile(currentName: String, newName: String) {

    }

    fun renameDir(currentName: String, newName: String) {

    }

    fun search(name: String) {
        println("SEARCHING FOR $name")
        searchTerm.value = name
        nodesSubscription = renterRepo.search(name, currentDirPath).siaSubscribe({
            println("GOT SEARCH RESULTS $it")
            displayedNodes.value = it
        }, ::onError)
    }

    fun cancelSearch() {
        searchTerm.value = null
        println("SEARCH CANCELED, DISPLAYING $currentDirPath")
        nodesSubscription = renterRepo.immediateNodes(currentDirPath).siaSubscribe({ nodes ->
            displayedNodes.value = nodes
        }, ::onError)
    }

    private fun onError(err: SiaError) {
        error.value = err
        error.value = null
    }
}