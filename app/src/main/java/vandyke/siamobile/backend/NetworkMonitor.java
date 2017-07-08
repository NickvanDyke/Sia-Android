/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import vandyke.siamobile.MainActivity;

public class NetworkMonitor extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI
                || MainActivity.prefs.getBoolean("runNodeOffWifi", false)) {
            context.startService(new Intent(context, Siad.class));
        } else {
            context.stopService(new Intent(context, Siad.class));
        }
    }
}
