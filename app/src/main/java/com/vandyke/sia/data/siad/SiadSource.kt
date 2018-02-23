/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.siad

import android.app.Activity
import android.app.Application
import android.content.*
import android.net.ConnectivityManager
import android.os.Bundle
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.util.rx.NonNullLiveData
import com.vandyke.sia.util.rx.SingleLiveEvent
import javax.inject.Inject
import javax.inject.Singleton

/** This class is used as a singleton that aggregates all factors of whether siad should be running */
@Singleton
class SiadSource
@Inject constructor(val application: Application) {

    val allConditionsGood = NonNullLiveData(false)
    val restart = SingleLiveEvent<Boolean>()

    private var activeNetworkType: Int? = null
        set(value) {
            field = value
            setConditions()
        }

    private var appInForeground = true /* initial value is true because onActivityResumed was already called for MainActivity at this point */
        set(value) {
            field = value
            setConditions()
        }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            "runSiaOnData", "runSiaInBackground", "siaManuallyStopped" -> setConditions()
            "useExternal", "apiPassword" -> restart.value = true
        }
    }

    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity?) {
            appInForeground = false
        }

        override fun onActivityResumed(activity: Activity?) {
            appInForeground = true
        }

        override fun onActivityStarted(activity: Activity?) {}
        override fun onActivityDestroyed(activity: Activity?) {}
        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
        override fun onActivityStopped(activity: Activity?) {}
        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ConnectivityManager.CONNECTIVITY_ACTION -> {
                    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetwork = cm.activeNetworkInfo
                    activeNetworkType = activeNetwork?.type
                }
                STOP_SIAD -> Prefs.siaManuallyStopped = true
            }
        }
    }

    init {
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(START_SIAD)
        filter.addAction(STOP_SIAD)
        application.registerReceiver(receiver, filter)
    }

    fun onDestroy() {
        Prefs.preferences.unregisterOnSharedPreferenceChangeListener(prefsListener)
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        application.unregisterReceiver(receiver)
    }

    private fun setConditions() {
        allConditionsGood.value = checkConditions()
    }

    private fun checkConditions(): Boolean = when {
        activeNetworkType == ConnectivityManager.TYPE_MOBILE && !Prefs.runSiaOnData -> false
        !appInForeground && !Prefs.runSiaInBackground -> false
        Prefs.siaManuallyStopped -> false
        else -> true
    }

    /* broadcast intents */
    companion object {
        val START_SIAD = "start_siad"
        val STOP_SIAD = "stop_siad"
    }
}