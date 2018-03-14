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

    init {
        // TODO: these don't set sizes - seems that listFiles() on the dirs returns null at this point?
        // delaying by 2000ms didn't help either. Results in reporting 0 size until
        // fileobserver is triggered
//            updateModule(module, internalSize = internalDir.recursiveLength())
//            updateModule(module, externalSize = externalDir.recursiveLength())
    }

    fun startWatching() {
        observers.forEach { it.startWatching() }
    }

    fun stopWatching() {
        observers.forEach { it.stopWatching() }
    }
}