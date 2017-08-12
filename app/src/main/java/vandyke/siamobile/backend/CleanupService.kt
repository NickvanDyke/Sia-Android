/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import vandyke.siamobile.prefs

class CleanupService : Service() {

    override fun onCreate() {
        //        Thread thread = new Thread() {
        //            public void run() {
        //
        //            }
        //        };
        //        thread.start();
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onDestroy() {

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!prefs.runInBackground) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
            stopSelf()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
