/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.siad

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import vandyke.siamobile.prefs

class SiadMonitorService : Service() {

    private var statusReceiver: StatusReceiver? = null

    override fun onCreate() {
        object : Thread() {
            override fun run() {
                statusReceiver = StatusReceiver()
                val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
                val handlerThread = HandlerThread("StatusReceiver")
                handlerThread.start()
                val looper = handlerThread.looper
                val handler = Handler(looper)
                registerReceiver(statusReceiver, intentFilter, null, handler)
            }
        }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onDestroy() {
        stopService(Intent(this, Siad::class.java))
        unregisterReceiver(statusReceiver)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!prefs.runInBackground) {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
