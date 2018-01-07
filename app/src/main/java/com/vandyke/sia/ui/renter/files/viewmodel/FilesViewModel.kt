/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.SiaError
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.repository.FilesRepositoryTest
import com.vandyke.sia.isSiadLoaded
import com.vandyke.sia.util.siaSubscribe
import io.reactivex.disposables.Disposable

class FilesViewModel : ViewModel() {
    val displayedNodes = MutableLiveData<List<Node>>()
    val currentDir = MutableLiveData<Dir>()
    val searching = MutableLiveData<Boolean>()
    val detailsItem = MutableLiveData<Node>()
    val error = MutableLiveData<SiaError>()

    val currentDirPath
        get() = currentDir.value?.path ?: ""

    private val filesRepo = FilesRepositoryTest()

    private val subscription = isSiadLoaded.subscribe {
        if (it)
            refresh()
    }

    /** the subscription to the database flowable that emits items in the current path */
    private var nodesSubscription: Disposable? = null
        set(value) {
            field?.dispose()
            field = value
        }

    init {
        displayedNodes.value = listOf()
        changeDir("root")
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    fun refresh() {
        filesRepo.updateFilesAndDirs().siaSubscribe({}, ::onError)
        // TODO: check that current directory is still valid. and track/display progress of update
    }

    fun changeDir(path: String) {
        println("changing to dir: $path")
        filesRepo.getDir(path).siaSubscribe({
            currentDir.value = it
        }, ::onError)
        /* note that changing directory will essentially cancel an active search. This is intended */
        searching.value = false
        nodesSubscription = filesRepo.immediateNodes(path).siaSubscribe({ nodes ->
            displayedNodes.value = nodes
        }, ::onError)

    }

    fun goToIndexInPath(index: Int) {
        val splitPath = currentDirPath.split("/")
        changeDir(splitPath.subList(0, index + 1).joinToString("/"))
    }

    fun goUpDir(): Boolean {
        val parent = currentDir.value?.parent
        return if (parent == null) {
            false
        } else {
            changeDir(parent)
            true
        }
    }

    fun displayDetails(node: Node) {
        detailsItem.value = node
    }

    /**
     * Creates a new directory with the given name in the current directory
     */
    fun createDir(name: String) = filesRepo.createDir("$currentDirPath/$name").siaSubscribe({}, ::onError)

    fun deleteDir(path: String) = filesRepo.deleteDir(path).siaSubscribe({}, ::onError)

    fun deleteFile(path: String) = filesRepo.deleteFile(path).siaSubscribe(::refresh, ::onError)

    fun addFile(path: String) {
        filesRepo.addFile(path, "blah", 10, 20).siaSubscribe(::refresh, ::onError)
    }

    fun renameFile(currentName: String, newName: String) {
        // TODO
    }

    fun renameDir(currentName: String, newName: String) {
        // TODO
    }

    fun search(name: String) {
        searching.value = true
        nodesSubscription = filesRepo.search(name, currentDirPath).siaSubscribe({
            println("GOT SEARCH RESULTS $it")
            displayedNodes.value = it
        }, ::onError)
    }

    fun cancelSearch() {
        searching.value = false
        nodesSubscription = filesRepo.immediateNodes(currentDirPath).siaSubscribe({
            displayedNodes.value = it
        }, ::onError)
    }

    private fun onError(err: SiaError) {
        error.value = err
        error.value = null
    }
}