/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.node.modules

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import com.vandyke.sia.data.local.Prefs

class NodeModulesViewModel(app: Application) : AndroidViewModel(app) {
    val modules = MutableLiveData<List<ModuleData>>()

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == "modulesString") {

        }
    }

    init {
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onCleared() {
        super.onCleared()
        Prefs.preferences.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }
}