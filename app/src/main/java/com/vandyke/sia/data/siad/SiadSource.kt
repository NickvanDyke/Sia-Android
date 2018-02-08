/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.siad

import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.util.rx.NonNullLiveData
import javax.inject.Inject
import javax.inject.Singleton

/** This class is used as a singleton that holds the state of siad and whether it should be running */
@Singleton
class SiadSource
@Inject constructor() {

    val siadOutput = MutableLiveData<String>()
    val isSiadLoaded = NonNullLiveData(false)

    val allConditionsGood = NonNullLiveData(false)

    var activeNetworkType: Int? = null
        set(value) {
            field = value
            setConditions()
        }

    var appInForeground = false
        set(value) {
            field = value
            setConditions()
        }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            "runSiaOnData" -> setConditions()
            "runSiaInBackground" -> setConditions()
        }
    }

    init {
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun setConditions() {
        allConditionsGood.value = checkConditions()
    }

    private fun checkConditions(): Boolean {
        return when {
            activeNetworkType == ConnectivityManager.TYPE_MOBILE && !Prefs.runSiaOnData -> false
            !appInForeground && !Prefs.runSiaInBackground -> false
            else -> true
        }
    }
}