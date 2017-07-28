/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.settings

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.BatteryManager
import vandyke.siamobile.backend.coldstorage.ColdStorageService
import vandyke.siamobile.backend.siad.Siad
import vandyke.siamobile.backend.siad.SiadMonitorService
import vandyke.siamobile.backend.wallet.WalletMonitorService
import vandyke.siamobile.prefs

class GlobalPrefsListener(private val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "operationMode" -> {
                when (prefs.operationMode) {
                    "cold_storage" -> {
                        prefs.address = "localhost:9990"
                        context.stopService(Intent(context, SiadMonitorService::class.java))
                        context.startService(Intent(context, ColdStorageService::class.java))
                    }
                    "remote_full_node" -> {
                        prefs.address = prefs.remoteAddress
                        context.stopService(Intent(context, ColdStorageService::class.java))
                        context.stopService(Intent(context, SiadMonitorService::class.java))
                    }
                    "local_full_node" -> {
                        prefs.address = "localhost:9980"
                        context.stopService(Intent(context, ColdStorageService::class.java))
                        context.startService(Intent(context, SiadMonitorService::class.java))
                    }
                }
                Thread {
                    try {
                        Thread.sleep(1000) // sleep for 1 second to give whatever service/server was started time to start before querying it
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    WalletMonitorService.staticRefresh()
                }.start()
            }
            "monitorRefreshInterval" -> WalletMonitorService.staticPostRunnable()
            "runLocalNodeOffWifi" -> {
                if (prefs.operationMode == "local_full_node") {
                    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetInfo = connectivityManager.activeNetworkInfo
                    if (activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI || prefs.runLocalNodeOffWifi) {
                        context.startService(Intent(context, Siad::class.java))
                    } else {
                        context.stopService(Intent(context, Siad::class.java))
                    }
                }
            }
            "localNodeMinBattery" -> {
                if (prefs.operationMode == "local_full_node") {
                    val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    if (level >= prefs.localNodeMinBattery)
                        context.startService(Intent(context, Siad::class.java))
                    else
                        context.stopService(Intent(context, Siad::class.java))
                }
            }
            "remoteAddress" -> if (prefs.operationMode == "remote_full_node") {
                prefs.address = prefs.remoteAddress
                WalletMonitorService.staticRefresh()
            }
        }
    }
}
