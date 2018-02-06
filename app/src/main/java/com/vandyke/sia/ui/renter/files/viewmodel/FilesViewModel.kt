/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.viewmodel

import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.data.repository.FilesRepository
import com.vandyke.sia.util.*
import io.reactivex.disposables.Disposable
import java.math.BigDecimal
import javax.inject.Inject

class FilesViewModel
@Inject constructor(
        private val filesRepository: FilesRepository
) : ViewModel() {
    val displayedNodes = NonNullLiveData<List<Node>>(listOf())
    val currentDir = NonNullLiveData<Dir>(Dir("", BigDecimal.ZERO))

    val searching = NonNullLiveData(false)
    val searchTerm = NonNullLiveData("") // maybe bind this to the search query?

    val ascending = NonNullLiveData(Prefs.ascending)
    val orderBy = NonNullLiveData(Prefs.orderBy)

    val activeTasks = NonNullLiveData(0)
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
            Prefs.ascending = it
            setDisplayedNodes()
        }
        orderBy.observeForevs {
            Prefs.orderBy = it
            setDisplayedNodes()
        }
        changeDir()
    }

    override fun onCleared() {
        super.onCleared()
        nodesSubscription?.dispose()
    }

    fun refresh() {
        activeTasks.increment()
        refreshing.value = true
        this.filesRepository.updateFilesAndDirs().io().main().subscribe({
            refreshing.value = false
        }, {
            refreshing.value = false
            onError(it)
            activeTasks.decrementZeroMin()
        })
        // TODO: check that current directory is still valid. and track/display progress of update
    }

    fun changeDir(path: String = "") {
        if (path == currentDirPath)
            return
        println("changing to dir: $path")
        this.filesRepository.getDir(path).io().main().subscribe({
            // maybe this would be better as a flowable
            currentDir.value = it
            setDisplayedNodes()
        }, {
            /* presumably the only error would be an empty result set from querying for the dir. In which case we go home */
            changeDir()
            onError(it)
        })
    }

    /** subscribes to the proper source for the displayed nodes, depending on the state of the viewmodel */
    private fun setDisplayedNodes() {
        if (searching.value) {
            nodesSubscription = this.filesRepository.search(searchTerm.value, currentDirPath, orderBy.value, ascending.value).io().main().subscribe({
                displayedNodes.value = it
            }, ::onError)
        } else {
            nodesSubscription = this.filesRepository.immediateNodes(currentDirPath, orderBy.value, ascending.value).io().main().subscribe({ nodes ->
                displayedNodes.value = nodes
            }, ::onError)
        }
    }

    fun goToIndexInPath(index: Int) {
        if (index == 0) {
            changeDir()
        } else {
            val splitPath = currentDirPath.split("/")
            val path = splitPath.subList(0, index).joinToString("/")
            changeDir(path)
        }
    }

    /** Creates a new directory with the given name in the current directory */
    fun createDir(name: String) = this.filesRepository.createDir(
            "${if (currentDirPath.isNotEmpty()) "$currentDirPath/" else ""}$name"
    ).io().main().subscribe({}, ::onError)

    fun deleteDir(dir: Dir) = this.filesRepository.deleteDir(dir.path).io().main().subscribe({}, ::onError)

    fun deleteFile(path: String) = this.filesRepository.deleteFile(path).io().main().subscribe(::refresh, ::onError)

    fun addFile(path: String, source: String) {
        this.filesRepository.addFile(path, source, 10, 20).io().main().subscribe(::refresh, ::onError)
    }

    fun renameFile(file: RenterFileData, newName: String) = this.filesRepository.moveFile(file.path, "${file.parent}/$newName")

    fun renameDir(dir: Dir, newName: String) {
        this.filesRepository.moveDir(dir.path, "${dir.parent!!}/$newName")
                .io().main().subscribe({}, ::onError)
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

    private fun onError(t: Throwable) {
        error.value = t
    }

    fun goUpDir(): Boolean {
        changeDir(currentDir.value.parent ?: return false)
        return true
    }
}