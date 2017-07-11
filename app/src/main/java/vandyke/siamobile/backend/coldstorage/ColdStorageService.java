/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.coldstorage;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import vandyke.siamobile.SiaMobileApplication;

import java.io.IOException;

public class ColdStorageService extends Service {

    private ColdStorageHttpServer coldStorageHttpServer;

    @Override
    public void onCreate() {
        Thread thread = new Thread() {
            public void run() {
                coldStorageHttpServer = new ColdStorageHttpServer(ColdStorageService.this);
                try {
                    coldStorageHttpServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (coldStorageHttpServer != null)
            coldStorageHttpServer.stop();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (!SiaMobileApplication.prefs.getBoolean("runColdStorageInBackground", false)) {
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
