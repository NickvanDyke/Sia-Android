/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.siad

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.BatteryManager
import vandyke.siamobile.prefs

class StatusReceiver : BroadcastReceiver() {

    private var batteryGood: Boolean = false
    private var networkGood: Boolean = false

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            batteryGood = level >= prefs.localNodeMinBattery
        } else if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetInfo = connectivityManager.activeNetworkInfo
            networkGood = activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI || prefs.runLocalNodeOffWifi
        }

        if (batteryGood && networkGood) {
            context.startService(Intent(context, Siad::class.java))
        } else {
            context.stopService(Intent(context, Siad::class.java))
        }
    }
}
