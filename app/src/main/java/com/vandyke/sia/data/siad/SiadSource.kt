/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.siad

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.util.NonNullLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiadSource
@Inject constructor(app: Application) {

    val siadOutput = MutableLiveData<String>()
    val isSiadLoaded = NonNullLiveData(false)

    val allConditionsGood = NonNullLiveData(false)

    val activeNetworkType = MutableLiveData<Int>()

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            "runSiaOnData" -> setConditions()
        }
    }

    init {
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        activeNetworkType.value = activeNetwork?.type

        activeNetworkType.observeForever {
            setConditions()
        }

        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun setConditions() {
        allConditionsGood.value = checkConditions()
    }

    private fun checkConditions(): Boolean {
        if (activeNetworkType.value == ConnectivityManager.TYPE_MOBILE && !Prefs.runSiaOnData)
            return false

        return true
    }
}