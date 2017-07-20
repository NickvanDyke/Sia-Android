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
import vandyke.siamobile.SiaMobileApplication;
import vandyke.siamobile.backend.coldstorage.ColdStorageService;
import vandyke.siamobile.backend.siad.SiadMonitorService;
import vandyke.siamobile.backend.wallet.WalletMonitorService;

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

        if (SiaMobileApplication.prefs.getBoolean("runInBackground", false)) {
            if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("cold_storage"))
                context.startService(new Intent(context, ColdStorageService.class));
            else if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("local_full_node"))
                context.startService(new Intent(context, SiadMonitorService.class));
            context.startService(new Intent(context, WalletMonitorService.class));
        }
    }
}
