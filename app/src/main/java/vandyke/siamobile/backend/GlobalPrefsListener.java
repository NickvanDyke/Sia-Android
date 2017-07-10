/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend;

import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.IBinder;
import vandyke.siamobile.backend.coldstorage.ColdStorageService;
import vandyke.siamobile.backend.siad.Siad;
import vandyke.siamobile.backend.siad.SiadMonitorService;
import vandyke.siamobile.misc.SiaMobileApplication;

public class GlobalPrefsListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context context;
    private ServiceConnection connection;
    private WalletMonitorService walletMonitorService;
    private boolean bound;

    public GlobalPrefsListener(Context context) {
        this.context = context;
        connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                walletMonitorService = ((WalletMonitorService.LocalBinder) service).getService();
                bound = true;
                walletMonitorService.postRefreshRunnable();
            }

            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (key) {
            case "operationMode":
                if (sharedPreferences.getString("operationMode", "cold_storage").equals("cold_storage")) {
                    if (editor.putString("address", "localhost:9990").commit()) {
                        context.stopService(new Intent(context, SiadMonitorService.class));
                        context.startService(new Intent(context, ColdStorageService.class));
                    }
                } else if (sharedPreferences.getString("operationMode", "cold_storage").equals("remote_full_node")) {
                    if (editor.putString("address", sharedPreferences.getString("remoteAddress", "192.168.1.11:9980")).commit()) {
                        context.stopService(new Intent(context, ColdStorageService.class));
                        context.stopService(new Intent(context, SiadMonitorService.class));
                    }
                } else if (sharedPreferences.getString("operationMode", "cold_storage").equals("local_full_node")) {
                    if (editor.putString("address", "localhost:9980").commit()) {
                        context.stopService(new Intent(context, ColdStorageService.class));
                        context.startService(new Intent(context, SiadMonitorService.class));
                    }
                }
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(1000); // sleep for 1 second to give whatever service/server was started time to start before querying it
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        refreshWalletMonitorService();
                    }
                }).start();
                break;
            case "monitorRefreshInterval":
                if (!bound)
                    context.bindService(new Intent(context, WalletMonitorService.class), connection, Context.BIND_AUTO_CREATE);
                else
                    walletMonitorService.postRefreshRunnable();
                break;
            case "runLocalNodeOffWifi":
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

                if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI
                        || SiaMobileApplication.prefs.getBoolean("runLocalNodeOffWifi", false)) {
                    context.startService(new Intent(context, Siad.class));
                } else {
                    context.stopService(new Intent(context, Siad.class));
                }
                break;
            case "localNodeMinBattery":
                Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                if (level >= Integer.parseInt(SiaMobileApplication.prefs.getString("localNodeMinBattery", "20")))
                    context.startService(new Intent(context, Siad.class));
                else
                    context.stopService(new Intent(context, Siad.class));
                break;
            case "remoteAddress":
                if (sharedPreferences.getString("operationMode", "cold_storage").equals("remote_full_node")) {
                    editor.putString("address", sharedPreferences.getString("remoteAddress", "192.168.1.11:9980"));
                    if (editor.commit())
                        refreshWalletMonitorService();
                }
                break;
        }
    }

    private void refreshWalletMonitorService() {
        if (!bound)
            context.bindService(new Intent(context, WalletMonitorService.class), connection, Context.BIND_AUTO_CREATE);
        else
            walletMonitorService.refreshAll();
    }
}
