package com.vandyke.sia.ui.node.modules

import android.arch.lifecycle.MutableLiveData
import android.os.FileObserver

class ModuleObserver(moduleData: ModuleData, notifier: MutableLiveData<Module>) {
    private val observers: List<FileObserver> = moduleData.directories.map {
        object : FileObserver(it.absolutePath) {
            override fun onEvent(event: Int, path: String?) {
                notifier.postValue(moduleData.type)
            }
        }
    }

    fun startWatching() {
        observers.forEach { it.startWatching() }
    }

    fun stopWatching() {
        observers.forEach { it.stopWatching() }
    }
}