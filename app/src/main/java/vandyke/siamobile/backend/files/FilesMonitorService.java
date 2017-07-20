/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.files;

import vandyke.siamobile.backend.BaseMonitorService;
import vandyke.siamobile.backend.wallet.WalletMonitorService;

import java.util.ArrayList;

public class FilesMonitorService extends BaseMonitorService {

    private static FilesMonitorService instance;

    private ArrayList<FilesUpdateListener> listeners;

    public void refresh() {

    }

    public void onCreate() {
        listeners = new ArrayList<>();
        instance = this;
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public interface FilesUpdateListener {

    }

    public void registerListener(FilesUpdateListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(FilesUpdateListener listener) {
        listeners.remove(listener);
    }

    public static void staticRefresh() {
        if (instance != null)
            instance.refresh();
    }

    public static void staticPostRunnable() {
        if (instance != null)
            instance.postRefreshRunnable();
    }
}
