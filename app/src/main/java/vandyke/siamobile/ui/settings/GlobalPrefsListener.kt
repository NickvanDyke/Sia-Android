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
import android.net.ConnectivityManager
import android.os.BatteryManager
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import vandyke.siamobile.backend.coldstorage.ColdStorageService
import vandyke.siamobile.backend.networking.SiaApi
import vandyke.siamobile.backend.siad.Siad
import vandyke.siamobile.backend.siad.SiadMonitorService
import vandyke.siamobile.backend.wallet.WalletService
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
                async(CommonPool) {
                    runBlocking { delay(1000) } // sleep for 1 second to give whatever service/server was started time to start before querying it
                    WalletService.singleAction(this@GlobalPrefsListener.context, { it.refresh() })
                }.start()
            }
            "refreshInterval" -> WalletService.singleAction(context, { it.postRefreshRunnable() })
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
            "address" -> SiaApi.rebuildApi()
            "remoteAddress" -> if (prefs.operationMode == "remote_full_node") {
                prefs.address = prefs.remoteAddress
                WalletService.singleAction(context, { it.refresh() })
            }
        }
    }
}
