/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.node.modules

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.SharedPreferences
import android.os.FileObserver
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.util.ExternalStorageException
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.rx.NonNullLiveData
import java.io.File
import java.math.BigDecimal

class NodeModulesViewModel(app: Application) : AndroidViewModel(app) {
    val modules = NonNullLiveData(listOf(
            ModuleData(Module.WALLET, Prefs.modulesString.contains('w', true)),
            ModuleData(Module.CONSENSUS, Prefs.modulesString.contains('c', true)),
            ModuleData(Module.TRANSACTION_POOL, Prefs.modulesString.contains('t', true)),
            ModuleData(Module.RENTER, Prefs.modulesString.contains('r', true)),
            ModuleData(Module.GATEWAY, Prefs.modulesString.contains('g', true))))

    private val observers: List<ModuleObserver> = List(modules.value.size, { index -> ModuleObserver(modules.value[index].type) })

    private val internalRootPath = app.filesDir.absolutePath
    private val externalRootPath = try {
        StorageUtil.getExternalStorage(app).absolutePath
    } catch (e: ExternalStorageException) {
        "" // is this correct?
    }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == "modulesString") {
            val modulesString = Prefs.modulesString
            updateModule(Module.CONSENSUS, on = modulesString.contains('c', true))
            updateModule(Module.WALLET, on = modulesString.contains('w', true))
            updateModule(Module.TRANSACTION_POOL, on = modulesString.contains('t', true))
            updateModule(Module.RENTER, on = modulesString.contains('r', true))
            updateModule(Module.GATEWAY, on = modulesString.contains('g', true))
        }
    }

    init {
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)


    }

    override fun onCleared() {
        super.onCleared()
        Prefs.preferences.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun updateModule(module: Module, on: Boolean? = null, internalSize: BigDecimal? = null, externalSize: BigDecimal? = null, cachedSize: BigDecimal? = null) {
        val currentList = modules.value
        modules.value = currentList.toMutableList().apply {
            // might have to post value instead
            val index = indexOfFirst { it.type == module }
            val current = this[index]
            if (on != null && internalSize != null) {
                this[index] = current.copy(on = on, internalSize = internalSize)
            } else if (on != null) {
                this[index] = current.copy(on = on)
            } else if (internalSize != null) {
                this[index] = current.copy(internalSize = internalSize)
            } else if (externalSize != null) {
                this[index] = current.copy(externalSize = externalSize)
            }
        }
    }

    fun deleteModule(module: Module, internal: Boolean) {
        File(if (internal) internalRootPath else externalRootPath + "/${module.text.toLowerCase()}").deleteRecursively()
    }

    inner class ModuleObserver(val module: Module) {
        private val moduleName = module.name.toLowerCase()

        private val internalDir = File("$internalRootPath/$moduleName")
        private val externalDir = File("$externalRootPath/$moduleName")

        private val internalDirObserver = object : FileObserver(internalDir.absolutePath) {
            override fun onEvent(event: Int, path: String?) {
                updateModule(module, internalSize = internalDir.length().toBigDecimal())
            }
        }
        private val externalDirObserver = object : FileObserver(externalDir.absolutePath) {
            override fun onEvent(event: Int, path: String?) {
                updateModule(module, externalSize = externalDir.length().toBigDecimal())
            }
        }

        init {
            updateModule(module, internalSize = internalDir.length().toBigDecimal())
            updateModule(module, externalSize = externalDir.length().toBigDecimal())

            internalDirObserver.startWatching()
            externalDirObserver.startWatching()
        }
    }
}