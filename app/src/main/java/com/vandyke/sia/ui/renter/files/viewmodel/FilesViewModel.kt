/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.viewmodel

import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.repository.FilesRepository
import com.vandyke.sia.data.repository.ROOT_DIR_NAME
import com.vandyke.sia.util.NonNullLiveData
import com.vandyke.sia.util.SingleLiveEvent
import com.vandyke.sia.util.io
import com.vandyke.sia.util.main
import io.reactivex.disposables.Disposable
import java.math.BigDecimal
import javax.inject.Inject

class FilesViewModel
@Inject constructor(
        private val filesRepository: FilesRepository
) : ViewModel() {
    val displayedNodes = NonNullLiveData<List<Node>>(listOf())
    val currentDir = NonNullLiveData<Dir>(Dir(ROOT_DIR_NAME, BigDecimal.ZERO))

    val searching = NonNullLiveData(false)
    val searchTerm = NonNullLiveData("") // maybe bind this to the search query?

    val ascending = NonNullLiveData(Prefs.ascending)
    val sortBy = NonNullLiveData(Prefs.sortBy)

    val refreshing = NonNullLiveData(false)
    val error = SingleLiveEvent<Throwable>()

    val currentDirPath
        get() = currentDir.value.path

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
        changeDir(ROOT_DIR_NAME)
    }

    override fun onCleared() {
        super.onCleared()
        nodesSubscription?.dispose()
    }

    fun refresh() {
        refreshing.value = true
        this.filesRepository.updateFilesAndDirs().io().main().subscribe({
            refreshing.value = false
        }, {
            refreshing.value = false
            onError(it)
        })
        // TODO: check that current directory is still valid. and track/display progress of update
    }

    fun changeDir(path: String) {
        if (path == currentDirPath)
            return
        println("changing to dir: $path")
        this.filesRepository.getDir(path).io().main().subscribe({ // maybe this would be better as a flowable
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
            nodesSubscription = this.filesRepository.search(searchTerm.value, currentDirPath, sortBy.value, ascending.value).io().main().subscribe({
                displayedNodes.value = it
            }, ::onError)
        } else {
            nodesSubscription = this.filesRepository.immediateNodes(currentDirPath, sortBy.value, ascending.value).io().main().subscribe({ nodes ->
                displayedNodes.value = nodes
            }, ::onError)
        }
    }

    fun goToIndexInPath(index: Int) {
        val splitPath = currentDirPath.split("/")
        changeDir(splitPath.subList(0, index + 1).joinToString("/"))
    }

    /** Creates a new directory with the given name in the current directory */
    fun createDir(name: String) = this.filesRepository.createDir("$currentDirPath/$name").io().main().subscribe({}, ::onError)

    fun deleteDir(path: String) = this.filesRepository.deleteDir(path).io().main().subscribe({}, ::onError)

    fun deleteFile(path: String) = this.filesRepository.deleteFile(path).io().main().subscribe(::refresh, ::onError)

    fun addFile(path: String) {
        this.filesRepository.addFile(path, "blah", 10, 20).io().main().subscribe(::refresh, ::onError)
    }

    fun renameFile(currentName: String, newName: String) {
        // TODO
    }

    fun renameDir(path: String, newName: String) = this.filesRepository.renameDir(path, newName).io().main().subscribe({}, ::onError)

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

    private fun onError(t: Throwable) {
        error.value = t
    }

    fun goUpDir(): Boolean {
        val parent = currentDir.value.parent
        return if (parent == null) {
            false
        } else {
            changeDir(parent)
            true
        }
    }
}