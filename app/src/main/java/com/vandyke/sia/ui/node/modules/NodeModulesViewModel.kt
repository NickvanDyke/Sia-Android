/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.node.modules

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.SharedPreferences
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadSource
import com.vandyke.sia.util.getAllFilesDirs
import com.vandyke.sia.util.rx.MutableSingleLiveEvent
import java.io.File
import javax.inject.Inject

// TODO: very rarely, when dagger attempts to create this class, it throws:
// Exception java.lang.RuntimeException: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.io.File.getAbsolutePath()' on a null object reference
class NodeModulesViewModel
@Inject constructor(
        context: Context,
        private val siadSource: SiadSource
) : ViewModel() {

    private val storageDirs = context.getAllFilesDirs()

    val modules = mutableListOf(
            ModuleData(Module.GATEWAY, Prefs.modulesString.contains('g', true), storageDirs.map { File(it.absolutePath, "gateway") }),
            ModuleData(Module.CONSENSUS, Prefs.modulesString.contains('c', true), storageDirs.map { File(it.absolutePath, "consensus") }),
            ModuleData(Module.TRANSACTIONPOOL, Prefs.modulesString.contains('t', true), storageDirs.map { File(it.absolutePath, "transactionpool") }),
            ModuleData(Module.WALLET, Prefs.modulesString.contains('w', true), storageDirs.map { File(it.absolutePath, "wallet") }),
            ModuleData(Module.RENTER, Prefs.modulesString.contains('r', true), storageDirs.map { File(it.absolutePath, "renter") }))

    val moduleUpdated = MutableSingleLiveEvent<Module>()

    val success = MutableSingleLiveEvent<String>()
    val error = MutableSingleLiveEvent<String>()

    private val moduleObservers: List<ModuleObserver> = modules.map { ModuleObserver(it, moduleUpdated) }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "modulesString") {
            modules.forEach {
                updateModuleEnabled(it.type, Prefs.modulesString.contains(it.type.name[0], true))
            }
        }
    }

    init {
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onCleared() {
        super.onCleared()
        Prefs.preferences.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    fun onShow() = moduleObservers.forEach { it.startWatching() }

    fun onHide() = moduleObservers.forEach { it.stopWatching() }

    private fun updateModuleEnabled(module: Module, enabled: Boolean) {
        val index = modules.indexOfFirst { it.type == module }
        modules[index] = modules[index].copy(enabled = enabled)
        moduleUpdated.value = module
    }

    fun deleteDir(module: Module, dir: File) {
        val result = dir.deleteRecursively()
        val shouldRestart = dir.parent == Prefs.siaWorkingDirectory
        if (result)
            success.value = "Deleted ${module.text} files from ${dir.absolutePath}${if (shouldRestart) ". Restarting Sia node..." else ""}"
        else
            error.value = "Error deleting ${module.text} files from ${dir.absolutePath}"
        if (shouldRestart)
            siadSource.signalRestart()
    }
}