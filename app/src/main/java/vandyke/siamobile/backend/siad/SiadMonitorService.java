/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.siad;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import vandyke.siamobile.backend.StatusReceiver;
import vandyke.siamobile.misc.SiaMobileApplication;

public class SiadMonitorService extends Service {

    private StatusReceiver statusReceiver;

    @Override
    public void onCreate() {
        new Thread() {
            public void run() {
                statusReceiver = new StatusReceiver();
                IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                HandlerThread handlerThread = new HandlerThread("StatusReceiver");
                handlerThread.start();
                Looper looper = handlerThread.getLooper();
                Handler handler = new Handler(looper);
                registerReceiver(statusReceiver, intentFilter, null, handler);
            }
        }.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, Siad.class));
        unregisterReceiver(statusReceiver);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (!SiaMobileApplication.prefs.getBoolean("runLocalNodeInBackground", false)) {
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
