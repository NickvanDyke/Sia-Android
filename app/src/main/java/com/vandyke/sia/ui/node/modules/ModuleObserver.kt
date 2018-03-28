package com.vandyke.sia.ui.node.modules

import android.os.FileObserver
import com.vandyke.sia.util.rx.MutableSingleLiveEvent

class ModuleObserver(moduleData: ModuleData, notifier: MutableSingleLiveEvent<Module>) {
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