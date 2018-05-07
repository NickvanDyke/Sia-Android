/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.viewmodel

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.persistence.room.EmptyResultSetException
import android.content.Intent
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.models.renter.*
import com.vandyke.sia.data.repository.FilesRepository
import com.vandyke.sia.data.siad.DownloadMonitorService
import com.vandyke.sia.util.pluralize
import com.vandyke.sia.util.rx.*
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import kotlin.properties.Delegates

class FilesViewModel
@Inject constructor(
        private val filesRepository: FilesRepository,
        private val application: Application
) : ViewModel() {
    val currentDir = MutableNonNullLiveData<Dir>(Dir("", 0))
    val displayedNodes = MutableNonNullLiveData<List<Node>>(listOf())

    val searching = MutableNonNullLiveData(false)
    val searchTerm = MutableNonNullLiveData("")

    val viewAsList = MutableNonNullLiveData(Prefs.viewAsList)
    val ascending = MutableNonNullLiveData(Prefs.ascending)
    val orderBy = MutableNonNullLiveData(Prefs.orderBy)

    val activeTasks = MutableNonNullLiveData(0)
    val refreshing = MutableNonNullLiveData(false)
    val success = MutableSingleLiveEvent<String>()
    val error = MutableSingleLiveEvent<Throwable>()

    val currentDirPath
        get() = currentDir.value.path

    val selectedNodes = MutableNonNullLiveData(listOf<Node>())
    val selecting
        get() = selectedNodes.value.isNotEmpty()
    val allSelectedAreInCurrentDir
        get() = selectedNodes.value.all { it.parent == currentDirPath }

    /** the subscription to the database flowable that emits items in the current path */
    private var nodesSubscription: Disposable? by Delegates.observable<Disposable?>(null) { _, oldValue, _ ->
        oldValue?.dispose()
    }

    init {
        viewAsList.observeForevs {
            Prefs.viewAsList = it
        }
        ascending.observeForevs {
            Prefs.ascending = it
            updateNodesSource()
        }
        orderBy.observeForevs {
            Prefs.orderBy = it
            updateNodesSource()
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
                    updateNodesSource()
                }, {
                    /* presumably the only error would be an empty result set from querying for the dir */
                    if (it is EmptyResultSetException) {
                        onError(EmptyResultSetException("No directory exists at path: $path"))
                    } else {
                        onError(it)
                    }
                    /* we set the value to itself so that the view will be notified and reset
                     * anything that changed from attempting to set the directory to a non-existent one,
                     * such as the selected item on the spinner */
                    currentDir.value = currentDir.value
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

    fun selectAllInCurrentDir() {
        selectedNodes.value = selectedNodes.value.union(displayedNodes.value).toList()
    }

    fun toggleSelect(node: Node) {
        if (selectedNodes.value.any { it.path == node.path })
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
                .subscribe({
                    /* start the service that will display notifications for active downloads. It'll stop itself when all are complete. */
                    application.startService(Intent(application, DownloadMonitorService::class.java))
                    val size = selectedNodes.value.size
                    success.value = "$size ${"file".pluralize(size)} will be downloaded. " +
                            "Check ${"notification".pluralize(size)} for details."
                    deselectAll()
                }, ::onError)
    }

    fun moveSelectedToCurrentDir() {
        // TODO: repository stuff should emit names of nodes as they're moved, so that we can
        // deselect them as they are. Would be useful for other multi methods too.
        // Currently if there's an error during moving (i.e. duplicate in destinate),
        // then some nodes might move before the error, but they won't be deselected.
        // This results in a "file doesn't exist" error when attempting to move the same
        // selection, since some of them have already moved, and so some of the selected nodes don't
        // exist anymore.
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

    fun uploadFile(source: String, redundancy: Float, name: String? = null) {
        val siapath = currentDirPath.withTrailingSlashIfNotEmpty() + (name ?: source.name())
        // TODO: actually calculate required pieces for the desired redundancy
        val pieces = when (redundancy) {
            else -> 10 to 20 /* first is datapieces, second is paritypieces. Redundancy = datapieces / (datapieces + paritypieces) */
//            else -> throw IllegalArgumentException()
        }
        filesRepository.uploadFile(siapath, source, pieces.first, pieces.second)
                .io()
                .main()
                .track(activeTasks)
                .subscribe(::refresh, ::onError)
    }

    fun renameFile(file: SiaFile, newName: String) {
        val parentPath = file.parent.withTrailingSlashIfNotEmpty()
        filesRepository.moveFile(file, "$parentPath$newName")
                .io()
                .main()
                .track(activeTasks)
                .subscribe({ deselect(file) }, ::onError)
    }

    fun renameDir(dir: Dir, newName: String) {
        val parentPath = dir.parent!!.withTrailingSlashIfNotEmpty()
        filesRepository.moveDir(dir, "$parentPath$newName")
                .io()
                .main()
                .track(activeTasks)
                .subscribe({ deselect(dir) }, ::onError)
    }

    fun search(name: String) {
        searching.value = true
        searchTerm.value = name
        updateNodesSource()
    }

    fun cancelSearch() {
        if (searching.value) {
            searching.value = false
            searchTerm.value = ""
            updateNodesSource()
            }
    }

    /** subscribes to the proper source for the displayed nodes, depending on the state of the viewmodel */
    private fun updateNodesSource() {
        nodesSubscription = when {
            searching.value -> filesRepository.search(searchTerm.value, currentDirPath, orderBy.value, ascending.value)
            else -> filesRepository.immediateNodes(currentDirPath, orderBy.value, ascending.value)
        }
                /* mapping is because we don't want to show the root dir which has an empty path */
                .map { nodes -> nodes.filterNot { node -> node is Dir && node.path.isEmpty() } }
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