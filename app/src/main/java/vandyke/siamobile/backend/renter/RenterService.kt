/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.renter

import vandyke.siamobile.backend.BaseMonitorService
import vandyke.siamobile.backend.networking.Renter
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.SiaError

class RenterService : BaseMonitorService() {
    var rootDir = SiaDir("root", null)

    private val listeners = ArrayList<FilesListener>()

    override fun refresh() {
        refreshFiles()
    }

    fun refreshFiles() {
        Renter.files(SiaCallback({ it ->
            rootDir = SiaDir("root", null)
            it.files.forEach { rootDir.addSiaFile(it) }
            sendFilesUpdate(rootDir)
        }, {
            sendFilesError(it)
        }))
    }

    interface FilesListener {
        fun onFilesUpdate(rootDir: SiaDir)
        fun onFilesError(error: SiaError)
    }

    fun registerListener(listener: FilesListener) = listeners.add(listener)

    fun unregisterListener(listener: FilesListener) = listeners.remove(listener)

    fun sendFilesUpdate(rootDir: SiaDir) = listeners.forEach { it.onFilesUpdate(rootDir) }

    fun sendFilesError(error: SiaError) = listeners.forEach { it.onFilesError(error) }
}