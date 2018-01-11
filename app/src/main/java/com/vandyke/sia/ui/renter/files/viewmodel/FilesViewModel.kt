/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.SiaError
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.remote.siaApi
import com.vandyke.sia.data.repository.FilesRepository
import com.vandyke.sia.data.repository.ROOT_DIR_NAME
import com.vandyke.sia.db
import com.vandyke.sia.isSiadLoaded
import com.vandyke.sia.util.NonNullLiveData
import com.vandyke.sia.util.siaSubscribe
import io.reactivex.disposables.Disposable

class FilesViewModel : ViewModel() {
    val displayedNodes = MutableLiveData<List<Node>>()
    val currentDir = MutableLiveData<Dir>()

    val searching = NonNullLiveData(false)
    val searchTerm = NonNullLiveData("") // maybe bind this to the search query?

    val ascending = NonNullLiveData(Prefs.ascending)
    val sortBy = NonNullLiveData(Prefs.sortBy)

    val error = MutableLiveData<SiaError>()

    val currentDirPath
        get() = currentDir.value?.path ?: ""

    private val filesRepo = FilesRepository(siaApi, db)

    private val subscription = isSiadLoaded.subscribe {
        if (it) refresh()
    }

    /** the subscription to the database flowable that emits items in the current path */
    private var nodesSubscription: Disposable? = null
        set(value) {
            field?.dispose()
            field = value
        }

    init {
        ascending.observeForevs {
            setDisplayedNodes()
            Prefs.ascending = it
        }
        sortBy.observeForevs {
            setDisplayedNodes()
            Prefs.sortBy = it
        }
        displayedNodes.value = listOf()
        changeDir(ROOT_DIR_NAME)
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
        nodesSubscription?.dispose()
    }

    fun refresh() {
        filesRepo.updateFilesAndDirs().siaSubscribe({}, ::onError)
        // TODO: check that current directory is still valid. and track/display progress of update
    }

    fun changeDir(path: String) {
        if (path == currentDirPath)
            return
        println("changing to dir: $path")
        filesRepo.getDir(path).siaSubscribe({
            currentDir.value = it
            setDisplayedNodes()
        }, {
            /* presumably the only error would be an empty result set from querying for the dir. In which case we go home */
            changeDir(ROOT_DIR_NAME)
            onError(it)
        })
    }

    /** subscribes to the proper source for the displayed nodes, depending on the state of the viewmodel */
    private fun setDisplayedNodes() {
        if (searching.value) {
            nodesSubscription = filesRepo.search(searchTerm.value!!, currentDirPath, sortBy.value, ascending.value).siaSubscribe({
                displayedNodes.value = it
            }, ::onError)
        } else {
            nodesSubscription = filesRepo.immediateNodes(currentDirPath, sortBy.value, ascending.value).siaSubscribe({ nodes ->
                displayedNodes.value = nodes
            }, ::onError)
        }
    }

    fun goToIndexInPath(index: Int) {
        val splitPath = currentDirPath.split("/")
        changeDir(splitPath.subList(0, index + 1).joinToString("/"))
    }

    /** Creates a new directory with the given name in the current directory */
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
        searchTerm.value = name
        setDisplayedNodes()
    }

    fun cancelSearch() {
        searching.value = false
        searchTerm.value = ""
        setDisplayedNodes()
    }

    private fun onError(err: SiaError) {
        error.value = err
        error.value = null
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
}