/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.viewmodel

import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.models.renter.Dir
import com.vandyke.sia.data.models.renter.Node
import com.vandyke.sia.data.models.renter.SiaFile
import com.vandyke.sia.data.models.renter.withTrailingSlashIfNotEmpty
import com.vandyke.sia.data.repository.FilesRepository
import com.vandyke.sia.util.rx.*
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class FilesViewModel
@Inject constructor(
        private val filesRepository: FilesRepository
) : ViewModel() {
    // TODO: show progress of operations
    val currentDir = NonNullLiveData<Dir>(Dir("", 0))
    val displayedNodes = NonNullLiveData<List<Node>>(listOf())

    val searching = NonNullLiveData(false)
    val searchTerm = NonNullLiveData("")

    val viewAsList = NonNullLiveData(Prefs.viewAsList)
    val ascending = NonNullLiveData(Prefs.ascending)
    val orderBy = NonNullLiveData(Prefs.orderBy)

    val activeTasks = NonNullLiveData(0)
    val refreshing = NonNullLiveData(false)
    val error = SingleLiveEvent<Throwable>()

    val currentDirPath
        get() = currentDir.value.path

    val selectedNodes = NonNullLiveData(listOf<Node>())
    val selecting
        get() = selectedNodes.value.isNotEmpty()
    val allSelectedAreInCurrentDir
        get() = selectedNodes.value.all { it.parent == currentDirPath }

    /** the subscription to the database flowable that emits items in the current path */
    private var nodesSubscription: Disposable? = null
        set(value) {
            field?.dispose()
            field = value
        }

    init {
        viewAsList.observeForevs {
            Prefs.viewAsList = it
        }
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
        filesRepository.updateFilesAndDirs()
                .io()
                .main()
                .track(activeTasks)
                .track(refreshing)
                .subscribe({}, ::onError)
        // TODO: check that current directory is still valid
    }

    fun changeDir(path: String = "") {
        if (path == currentDirPath)
            return
        println("changing to dir: $path")
        // maybe this would be better as a flowable. But that caused some other problems
        // when trying. And it also doesn't emit when the current dir no longer exists, which
        // would have been one of the main advantages
        this.filesRepository.getDir(path)
                .io()
                .main()
                .subscribe({
                    currentDir.value = it
                    setDisplayedNodes()
                }, {
                    /* presumably the only error would be an empty result set from querying for the dir. In which case we go home */
                    changeDir()
                    onError(it)
                })
    }

    fun select(node: Node) {
        val new = selectedNodes.value.toMutableList()
        new.add(node)
        selectedNodes.value = new.toList()
    }

    fun deselect(node: Node) {
        selectedNodes.value = selectedNodes.value.filterNot { it.path == node.path }
    }

    fun deselectAll() {
        selectedNodes.value = listOf()
    }

    fun toggleSelect(node: Node) {
        if (selectedNodes.value.find { it.path == node.path } != null)
            deselect(node)
        else
            select(node)
    }

    fun deleteSelected() {
        filesRepository.multiDelete(selectedNodes.value)
                .io()
                .main()
                .track(activeTasks)
                .subscribe(::deselectAll, ::onError)
    }

    fun downloadSelected(destination: String) {
        filesRepository.multiDownload(selectedNodes.value, destination)
                .io()
                .main()
                .track(activeTasks)
                .subscribe(::deselectAll, ::onError)
    }

    fun moveSelectedToCurrentDir() {
        filesRepository.multiMove(selectedNodes.value, currentDirPath)
                .io()
                .main()
                .track(activeTasks)
                .subscribe(::deselectAll, ::onError)
    }

    /** Creates a new directory with the given name in the current directory */
    fun createDir(name: String) {
        filesRepository.createDir("${currentDirPath.withTrailingSlashIfNotEmpty()}$name")
                .io()
                .main()
                .track(activeTasks)
                .subscribe({}, ::onError)
    }

    fun uploadFile(source: String) {
        val path = currentDirPath.withTrailingSlashIfNotEmpty() + source.substring(source.lastIndexOf('/') + 1)
        filesRepository.uploadFile(path, source, 10, 20)
                .io()
                .main()
                .track(activeTasks)
                .subscribe({}, ::onError)
    }

    fun renameFile(file: SiaFile, newName: String) {
        val parentPath = file.parent.withTrailingSlashIfNotEmpty()
        filesRepository.moveFile(file, "$parentPath$newName")
                .io()
                .main()
                .track(activeTasks)
                .subscribe({}, ::onError)
    }

    fun renameDir(dir: Dir, newName: String) {
        val parentPath = dir.parent!!.withTrailingSlashIfNotEmpty()
        filesRepository.moveDir(dir, "$parentPath$newName")
                .io()
                .main()
                .track(activeTasks)
                .subscribe({}, ::onError)
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

    /** subscribes to the proper source for the displayed nodes, depending on the state of the viewmodel */
    private fun setDisplayedNodes() {
        nodesSubscription =
                if (searching.value) {
                    filesRepository.search(searchTerm.value, currentDirPath, orderBy.value, ascending.value)
                } else {
                    filesRepository.immediateNodes(currentDirPath, orderBy.value, ascending.value)
                }
                        .io()
                        .main()
                        .subscribe(displayedNodes::setValue, ::onError)
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

    fun goUpDir(): Boolean {
        changeDir(currentDir.value.parent ?: return false)
        return true
    }

    private fun onError(t: Throwable) {
        error.value = t
    }
}