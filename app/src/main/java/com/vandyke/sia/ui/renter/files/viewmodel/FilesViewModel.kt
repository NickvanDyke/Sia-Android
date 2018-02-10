/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.viewmodel

import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.local.models.renter.withTrailingSlashIfNotEmpty
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.data.repository.FilesRepository
import com.vandyke.sia.util.rx.*
import io.reactivex.disposables.Disposable
import java.math.BigDecimal
import javax.inject.Inject

class FilesViewModel
@Inject constructor(
        private val filesRepository: FilesRepository
) : ViewModel() {
    val displayedNodes = NonNullLiveData<List<Node>>(listOf())
    val currentDir = NonNullLiveData<Dir>(Dir("", BigDecimal.ZERO))

    val selectedNodes = NonNullLiveData(listOf<Node>())

    val searching = NonNullLiveData(false)
    val searchTerm = NonNullLiveData("") // maybe bind this to the search query?

    val ascending = NonNullLiveData(Prefs.ascending)
    val orderBy = NonNullLiveData(Prefs.orderBy)

    val activeTasks = NonNullLiveData(0)
    val refreshing = NonNullLiveData(false)
    val error = SingleLiveEvent<Throwable>()

    val currentDirPath
        get() = currentDir.value.path

    val currentDirPathWithTrailingSlash
        get() = currentDirPath + if (currentDirPath.isNotEmpty()) "/" else ""


    val selecting
        get() = selectedNodes.value.isNotEmpty()

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
        // TODO: check that current directory is still valid
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

    fun select(node: Node) {
        val new = mutableListOf(node)
        new.addAll(selectedNodes.value)
        selectedNodes.value = new
    }

    fun deselect(node: Node) {
        selectedNodes.value = selectedNodes.value.filterNot { it.path == node.path }
    }

    fun toggleSelect(node: Node) {
        if (selectedNodes.value.find { it.path == node.path } != null)
            deselect(node)
        else
            select(node)
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
    fun createDir(name: String) {
        this.filesRepository.createDir("${currentDirPath.withTrailingSlashIfNotEmpty()}$name"
        ).io().main().subscribe({}, ::onError)
    }

    fun deleteDir(dir: Dir) {
        this.filesRepository.deleteDir(dir.path).io().main().subscribe({}, ::onError)
    }

    fun deleteFile(file: RenterFileData) {
        this.filesRepository.deleteFile(file.path).io().main().subscribe(::refresh, ::onError)
    }

    fun addFile(source: String) {
        val path = currentDirPath.withTrailingSlashIfNotEmpty() + source.substring(source.lastIndexOf('/') + 1)
        this.filesRepository.addFile(path, source, 10, 20).io().main().subscribe(::refresh, ::onError)
    }

    fun renameFile(file: RenterFileData, newName: String) {
        val parentPath = file.parent!!.withTrailingSlashIfNotEmpty()
        this.filesRepository.moveFile(file.path, "$parentPath$newName")
                .io().main().subscribe({}, ::onError)
    }

    fun renameDir(dir: Dir, newName: String) {
        val parentPath = dir.parent!!.withTrailingSlashIfNotEmpty()
        this.filesRepository.moveDir(dir.path, "$parentPath$newName")
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