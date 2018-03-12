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
import com.vandyke.sia.util.rx.SingleLiveEvent
import java.io.File
import javax.inject.Inject

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

    val moduleUpdated = SingleLiveEvent<Module>()

    val success = SingleLiveEvent<String>()
    val error = SingleLiveEvent<String>()

    private val moduleObservers: List<ModuleObserver> = modules.map { ModuleObserver(it, moduleUpdated) }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "modulesString") {
            val modulesString = Prefs.modulesString
            updateModuleEnabled(Module.GATEWAY, modulesString.contains('g', true))
            updateModuleEnabled(Module.CONSENSUS, modulesString.contains('c', true))
            updateModuleEnabled(Module.TRANSACTIONPOOL, modulesString.contains('t', true))
            updateModuleEnabled(Module.WALLET, modulesString.contains('w', true))
            updateModuleEnabled(Module.RENTER, modulesString.contains('r', true))
        }
    }

    init {
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onCleared() {
        super.onCleared()
        Prefs.preferences.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    fun onShow() {
        moduleObservers.forEach { it.startWatching() }
    }

    fun onHide() {
        moduleObservers.forEach { it.stopWatching() }
    }

    private fun updateModuleEnabled(module: Module, enabled: Boolean) {
        val index = modules.indexOfFirst { it.type == module }
        modules[index] = modules[index].copy(enabled = enabled)
        moduleUpdated.value = module
    }

    fun deleteDir(module: Module, dir: File) {
        val result = dir.deleteRecursively()
        if (result)
            success.value = "Deleted ${module.text} files from ${dir.absolutePath}. Restarting Sia node..."
        else
            error.value = "Error deleting ${module.text} files from ${dir.absolutePath}"
        siadSource.signalRestart()
    }
}