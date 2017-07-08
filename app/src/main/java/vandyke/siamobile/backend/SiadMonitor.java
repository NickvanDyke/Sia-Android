/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

public class SiadMonitor extends Service {

    private StatusReceiver statusReceiver;

    @Override
    public void onCreate() {
        statusReceiver = new StatusReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        HandlerThread handlerThread = new HandlerThread("StatusReceiver");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        registerReceiver(statusReceiver, intentFilter, null, handler);
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
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
    public IBinder onBind(Intent intent) {
        return null;
    }
}
