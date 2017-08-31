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
import vandyke.siamobile.backend.coldstorage.ColdStorageService
import vandyke.siamobile.backend.networking.SiaApi
import vandyke.siamobile.backend.siad.SiadService
import vandyke.siamobile.prefs

class GlobalPrefsListener(private val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "operationMode" -> {
                when (prefs.operationMode) {
                    "cold_storage" -> {
                        prefs.address = "localhost:9990"
                        context.stopService(Intent(context, SiadService::class.java))
                        context.startService(Intent(context, ColdStorageService::class.java))
                    }
                    "remote_full_node" -> {
                        prefs.address = prefs.remoteAddress
                        context.stopService(Intent(context, ColdStorageService::class.java))
                        context.stopService(Intent(context, SiadService::class.java))
                    }
                    "local_full_node" -> {
                        prefs.address = "localhost:9980"
                        context.stopService(Intent(context, ColdStorageService::class.java))
                        context.startService(Intent(context, SiadService::class.java))
                    }
                }
            }
            "refreshInterval" -> TODO("change job interval")
            "runLocalNodeOffWifi" -> {
                if (prefs.operationMode == "local_full_node") {
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
            "localNodeMinBattery" -> {
                if (prefs.operationMode == "local_full_node") {
                    val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    if (SiadService.isBatteryGood(batteryStatus))
                        SiadService.singleAction(context, {
                            it.startSiad()
                        })
                    else
                        SiadService.singleAction(context, {
                            it.siadNotification("Stopped - battery is below ${prefs.localNodeMinBattery}%")
                            it.stopSiad()
                        })
                }
            }
            "address" -> SiaApi.rebuildApi()
            "remoteAddress" -> if (prefs.operationMode == "remote_full_node") {
                prefs.address = prefs.remoteAddress
            }
        }
    }
}
