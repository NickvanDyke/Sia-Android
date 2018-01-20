/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.siad

import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.util.NonNullLiveData
import javax.inject.Inject
import javax.inject.Singleton

/** This class is used as a singleton that holds the state of siad and whether it should be running */
@Singleton
class SiadSource
@Inject constructor() {

    val siadOutput = MutableLiveData<String>()
    val isSiadLoaded = NonNullLiveData(false)

    val allConditionsGood = NonNullLiveData(false)

    val activeNetworkType = NonNullLiveData(ConnectivityManager.TYPE_MOBILE)

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            "runSiaOnData" -> setConditions()
        }
    }

    init {
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