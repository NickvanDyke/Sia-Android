package vandyke.siamobile.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import vandyke.siamobile.misc.SiaMobileApplication;

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("local_full_node")
                && SiaMobileApplication.prefs.getBoolean("runLocalNodeInBackground", false))
            context.startService(new Intent(context, SiadMonitor.class));
        if (SiaMobileApplication.prefs.getBoolean("monitorInBackground", true))
            context.startService(new Intent(context, WalletService.class));
    }
}
