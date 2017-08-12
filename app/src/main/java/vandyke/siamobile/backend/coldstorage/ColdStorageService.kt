/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.coldstorage

import android.app.Service
import android.content.Intent
import android.os.IBinder
import vandyke.siamobile.prefs

class ColdStorageService : Service() {

    private lateinit var coldStorageHttpServer: ColdStorageHttpServer

    override fun onCreate() {
        coldStorageHttpServer = ColdStorageHttpServer()
        coldStorageHttpServer.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onDestroy() {
        coldStorageHttpServer.stop()
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
