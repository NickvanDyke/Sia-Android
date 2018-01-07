/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.vandyke.sia.data.SiaError
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.repository.FilesRepositoryTest
import com.vandyke.sia.isSiadLoaded
import com.vandyke.sia.util.siaSubscribe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

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
        filesRepo.updateFilesAndDirs().subscribeOn(Schedulers.io()).subscribe()
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
    fun createDir(name: String) = filesRepo.createNewDir("$currentDirPath/$name").siaSubscribe(::refresh, ::onError)

    fun deleteDir(path: String) = filesRepo.deleteDir(path).siaSubscribe(::refresh, ::onError)

    fun deleteFile(path: String) = filesRepo.deleteFile(path).siaSubscribe(::refresh, ::onError)

    fun addFile(path: String) {
        filesRepo.addFile(path, "blah", 10, 20).siaSubscribe(::refresh, ::onError)
    }

    fun renameFile(currentName: String, newName: String) {

    }

    fun renameDir(currentName: String, newName: String) {

    }

    fun search(name: String) {
        Log.d("DEBUG", "SEARCHING FOR $name IN $currentDirPath")
        searching.value = true
        nodesSubscription = filesRepo.search(name, currentDirPath).siaSubscribe({
            println("GOT SEARCH RESULTS $it")
            displayedNodes.value = it
        }, ::onError)
    }

    fun cancelSearch() {
        searching.value = false
        Log.d("DEBUG", "SEARCH CANCELED, DISPLAYING $currentDirPath")
        nodesSubscription = filesRepo.immediateNodes(currentDirPath).siaSubscribe({
            displayedNodes.value = it
        }, ::onError)
    }

    private fun onError(err: SiaError) {
        error.value = err
        error.value = null
    }
}