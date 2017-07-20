/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.siad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import vandyke.siamobile.SiaMobileApplication;

public class StatusReceiver extends BroadcastReceiver {

    private boolean batteryGood;
    private boolean networkGood;

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            if (level >= Integer.parseInt(SiaMobileApplication.prefs.getString("localNodeMinBattery", "20"))) {
                batteryGood = true;
            } else
                batteryGood = false;
        } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI
                    || SiaMobileApplication.prefs.getBoolean("runLocalNodeOffWifi", false)) {
                networkGood = true;
            } else {
                networkGood = false;
            }
        }

        if (batteryGood && networkGood) {
            context.startService(new Intent(context, Siad.class));
        } else {
            context.stopService(new Intent(context, Siad.class));
        }
    }
}
