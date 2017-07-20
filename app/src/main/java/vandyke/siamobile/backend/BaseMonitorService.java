/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import vandyke.siamobile.SiaMobileApplication;

public abstract class BaseMonitorService extends Service {

    private final IBinder binder = new BaseMonitorService.LocalBinder();

    private Handler handler;
    private Runnable refreshRunnable;

    public abstract void refresh();

    public void postRefreshRunnable() {
        if (refreshRunnable != null)
            handler.removeCallbacks(refreshRunnable);
        final long refreshInterval = 60000 * Long.parseLong(SiaMobileApplication.prefs.getString("monitorRefreshInterval", "1"));
        refreshRunnable = new Runnable() {
            public void run() {
                refresh();
                handler.postDelayed(refreshRunnable, refreshInterval);
            }
        };
        if (refreshInterval != 0)
            handler.post(refreshRunnable);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void onCreate() {
        handler = new Handler();
        postRefreshRunnable();
    }

    public void onDestroy() {
        if (handler != null && refreshRunnable != null)
            handler.removeCallbacks(refreshRunnable);
    }

    public void onTaskRemoved(Intent rootIntent) {
        if (!SiaMobileApplication.prefs.getBoolean("runInBackground", false))
            stopSelf();
    }

    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public BaseMonitorService getService() {
            return BaseMonitorService.this;
        }
    }
}
