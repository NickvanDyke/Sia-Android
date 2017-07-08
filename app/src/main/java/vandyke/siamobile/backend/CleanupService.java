/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import vandyke.siamobile.MainActivity;

public class CleanupService extends Service {
    @Override
    public void onCreate() {
//        Thread thread = new Thread() {
//            public void run() {
//
//            }
//        };
//        thread.start();
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (MainActivity.prefs.getBoolean("runLocalNodeInBackground", false)) {
            stopService(new Intent(this, Siad.class));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
