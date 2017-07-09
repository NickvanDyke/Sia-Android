package vandyke.siamobile.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import vandyke.siamobile.backend.coldstorage.ColdStorageService;
import vandyke.siamobile.backend.siad.SiadMonitorService;
import vandyke.siamobile.misc.SiaMobileApplication;

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("local_full_node")
                && SiaMobileApplication.prefs.getBoolean("runLocalNodeInBackground", false))
            context.startService(new Intent(context, SiadMonitorService.class));
        else if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("cold_storage")
                && SiaMobileApplication.prefs.getBoolean("runColdStorageInBackground", false))
            context.startService(new Intent(context, ColdStorageService.class));
        if (SiaMobileApplication.prefs.getBoolean("monitorInBackground", true))
            context.startService(new Intent(context, WalletMonitorService.class));
    }
}
