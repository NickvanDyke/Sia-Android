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

/** This class is used as a singleton that aggregates all factors of whether siad should be running.
 * Should mostly only be used in SiadService, and maaaaybe elsewhere if triggering a restart is needed. */
@Singleton
class SiadSource
@Inject constructor(private val application: Application) {

    val allConditionsGood = NonNullLiveData(false)
    val restart = SingleLiveEvent<Boolean>()

    private var activeNetworkType: Int? = null
        set(value) {
            field = value
            setConditions()
        }

    // TODO: could have better detection here. For example, currently opening a Chrome custom tab makes this false
    // Might also be good to have a ~minute delay before shutting down the node once this changes, so that
    // it's not turning on and off a ton if the user is switching back and forth between this and other apps quickly
    // Should be easy to implement that in SiadService using Handler#postDelayed
    private var appInForeground = true /* initial value is true because onActivityResumed was already called for MainActivity at this point */
        set(value) {
            field = value
            setConditions()
        }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            "runSiaOnData", "runSiaInBackground", "siaManuallyStopped" -> setConditions()
            "siaWorkingDirectory", "apiPassword", "modulesString" -> signalRestart()
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

    /* methods to be called in the matching SiadService lifecycle callbacks */
    fun onCreate() {
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)

        application.registerReceiver(
                receiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                        .apply {
                            addAction(START_SIAD)
                            addAction(STOP_SIAD)
                        })
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

    fun signalRestart() {
        restart.value = true
    }

    /* broadcast intents */
    companion object {
        val START_SIAD = "START_SIAD"
        val STOP_SIAD = "STOP_SIAD"
    }
}