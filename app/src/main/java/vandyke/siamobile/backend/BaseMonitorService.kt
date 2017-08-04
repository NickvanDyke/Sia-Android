/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import vandyke.siamobile.prefs

abstract class BaseMonitorService : Service() {

    private val binder = LocalBinder()

    private var handler: Handler = Handler()
    private var refreshRunnable: Runnable? = null

    abstract fun refresh()

    fun postRefreshRunnable() {
        if (refreshRunnable != null)
            handler.removeCallbacks(refreshRunnable)
        val refreshInterval = 60000 * prefs.refreshInterval
        if (refreshInterval == 0)
            return
        refreshRunnable = Runnable {
            refresh()
            handler.postDelayed(refreshRunnable, refreshInterval.toLong())
        }
        handler.post(refreshRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onCreate() {
        postRefreshRunnable()
    }

    override fun onDestroy() {
        if (refreshRunnable != null)
            handler.removeCallbacks(refreshRunnable)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!prefs.runInBackground)
            stopSelf()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        val service: BaseMonitorService
            get() = this@BaseMonitorService
    }
}
