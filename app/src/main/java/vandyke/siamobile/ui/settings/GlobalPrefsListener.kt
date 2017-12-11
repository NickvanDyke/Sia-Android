/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.settings

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import vandyke.siamobile.data.local.Prefs
import vandyke.siamobile.data.remote.SiaApi
import vandyke.siamobile.data.siad.SiadService

class GlobalPrefsListener(private val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "operationMode" -> {
                when (Prefs.operationMode) {
                    "cold_storage" -> {
                        Prefs.address = "localhost:9990"
                        context.stopService(Intent(context, SiadService::class.java))
                    }
                    "remote_full_node" -> {
                        Prefs.address = Prefs.remoteAddress
                        context.stopService(Intent(context, SiadService::class.java))
                    }
                    "local_full_node" -> {
                        Prefs.address = "localhost:9980"
                        context.startService(Intent(context, SiadService::class.java))
                    }
                }
            }
            "runLocalNodeOffWifi" -> {
                if (Prefs.operationMode == "local_full_node") {
                    if (SiadService.isConnectionGood(context))
                        SiadService.singleAction(context, {
                            it.startSiad()
                        })
                    else
                        SiadService.singleAction(context, {
                            it.siadNotification("Stopped - not connected to WiFi")
                            it.stopSiad()
                        })
                }
            }
        // TODO: this is the cause of siad starting when wifi + battery requirements aren't met I think
            "localNodeMinBattery" -> {
                if (Prefs.operationMode == "local_full_node") {
                    val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    if (SiadService.isBatteryGood(batteryStatus))
                        SiadService.singleAction(context, {
                            it.startSiad()
                        })
                    else
                        SiadService.singleAction(context, {
                            it.siadNotification("Stopped - battery is below ${Prefs.localNodeMinBattery}%")
                            it.stopSiad()
                        })
                }
            }
            "address" -> SiaApi.rebuildApi()
            "remoteAddress" -> if (Prefs.operationMode == "remote_full_node") {
                Prefs.address = Prefs.remoteAddress
            }
            "SiaNodeWakeLock" -> SiadService.getService(context).subscribe { service ->
                /* If Siad is already running then we must tell the service to acquire/release its wake lock
                   because normally it does so in start/stopSiad() */
                if (service.isSiadRunning) {
                    if (Prefs.SiaNodeWakeLock)
                        service.wakeLock.acquire()
                    else if (service.wakeLock.isHeld)
                        service.wakeLock.release()
                }
            }
        }
    }
}
