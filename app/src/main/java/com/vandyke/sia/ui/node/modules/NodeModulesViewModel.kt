/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.node.modules

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.SharedPreferences
import android.os.FileObserver
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadSource
import com.vandyke.sia.util.ExternalStorageException
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.recursiveLength
import com.vandyke.sia.util.rx.NonNullLiveData
import com.vandyke.sia.util.rx.SingleLiveEvent
import java.io.File
import javax.inject.Inject

class NodeModulesViewModel
@Inject constructor(
        context: Context,
        val siadSource: SiadSource
) : ViewModel() {
    val modules = NonNullLiveData(listOf(
            ModuleData(Module.GATEWAY, Prefs.modulesString.contains('g', true)),
            ModuleData(Module.CONSENSUS, Prefs.modulesString.contains('c', true)),
            ModuleData(Module.TRANSACTIONPOOL, Prefs.modulesString.contains('t', true)),
            ModuleData(Module.WALLET, Prefs.modulesString.contains('w', true)),
            ModuleData(Module.RENTER, Prefs.modulesString.contains('r', true))))

    val success = SingleLiveEvent<String>()
    val error = SingleLiveEvent<String>()

    private val internalRootPath = context.filesDir.path
    private val externalRootPath = try {
        StorageUtil.getExternalStorage(context).path
    } catch (e: ExternalStorageException) {
        "" // is this correct?
    }

    private val observers: List<ModuleObserver> = List(modules.value.size, { index -> ModuleObserver(modules.value[index].type) })

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == "modulesString") {
            val modulesString = Prefs.modulesString
            updateModule(Module.GATEWAY, on = modulesString.contains('g', true))
            updateModule(Module.CONSENSUS, on = modulesString.contains('c', true))
            updateModule(Module.TRANSACTIONPOOL, on = modulesString.contains('t', true))
            updateModule(Module.WALLET, on = modulesString.contains('w', true))
            updateModule(Module.RENTER, on = modulesString.contains('r', true))
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
        observers.forEach { it.startWatching() }
    }

    fun onHide() {
        observers.forEach { it.stopWatching() }
    }

    private fun updateModule(module: Module, on: Boolean? = null, internalSize: Long? = null, externalSize: Long? = null) {
        val currentList = modules.value
        modules.postValue(currentList.toMutableList().apply {
            val index = indexOfFirst { it.type == module }
            val current = this[index]
            this[index] = when {
                on != null -> current.copy(on = on)
                internalSize != null -> current.copy(internalSize = internalSize)
                externalSize != null -> current.copy(externalSize = externalSize)
                else -> throw IllegalArgumentException()
            }
        })
    }

    fun deleteModule(module: Module, internal: Boolean) {
        val file = File((if (internal) internalRootPath else externalRootPath) + "/${module.name.toLowerCase()}")
        val result = file.deleteRecursively()
        if (result)
            success.value = "Deleted ${module.text} files from ${if (internal) "internal" else "external"} storage. Restarting Sia node..."
        else
            error.value = "Error deleting ${module.text} files from ${if (internal) "internal" else "external"} storage"
        siadSource.signalRestart()
    }

    inner class ModuleObserver(val module: Module) {
        private val moduleName = module.name.toLowerCase()

        private val internalDir = File("$internalRootPath/$moduleName")
        private val externalDir = File("$externalRootPath/$moduleName")

        private val internalDirObserver = object : FileObserver(internalDir.absolutePath) {
            override fun onEvent(event: Int, path: String?) {
                updateModule(module, internalSize = internalDir.recursiveLength())
            }
        }
        private val externalDirObserver = object : FileObserver(externalDir.absolutePath) {
            override fun onEvent(event: Int, path: String?) {
                updateModule(module, externalSize = externalDir.recursiveLength())
            }
        }

        init {
            // TODO: these don't set sizes - seems that listFiles() on the dirs returns null at this point?
            // delaying by 2000ms didn't help either. Results in reporting 0 size until
            // fileobserver is triggered
            updateModule(module, internalSize = internalDir.recursiveLength())
            updateModule(module, externalSize = externalDir.recursiveLength())
        }

        fun startWatching() {
            internalDirObserver.startWatching()
            externalDirObserver.startWatching()
        }

        fun stopWatching() {
            internalDirObserver.stopWatching()
            externalDirObserver.stopWatching()
        }
    }
}