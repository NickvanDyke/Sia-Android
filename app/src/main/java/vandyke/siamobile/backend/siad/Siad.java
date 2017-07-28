/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.siad;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.backend.wallet.WalletMonitorService;
import vandyke.siamobile.misc.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Siad extends Service {

    private int SIAD_NOTIFICATION = 3;

    private File siadFile;
    private java.lang.Process siadProcess;
    private Thread readStdoutThread;
//    final private StringBuilder stdoutBuffer = new StringBuilder();
//    private TerminalFragment terminalFragment;

//    public String getBufferedStdout() {
//        if (stdoutBuffer == null)
//            return "";
//        String result = stdoutBuffer.toString();
//        stdoutBuffer.setLength(0);
//        return result;
//    }
//
//    public void setTerminalFragment(TerminalFragment fragment) {
//        terminalFragment = fragment;
//    }
//
//    private void terminalAppend(String text) {
//        if (terminalFragment != null)
//            terminalFragment.appendToOutput(text);
//        else
//            stdoutBuffer.append(text);
//    }

    @Override
    public void onCreate() {
        startForeground(SIAD_NOTIFICATION, buildSiadNotification("Starting..."));
        Thread thread = new Thread(() -> {
            siadFile = Utils.INSTANCE.copyBinary("siad", Siad.this, false);
            if (siadFile == null) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Utils.INSTANCE.notification(Siad.this, SIAD_NOTIFICATION, R.drawable.ic_dns_white_48dp,
                                "Local full node", "Unsupported CPU architecture", false);
                    }
                });
                stopForeground(true);
                stopSelf();
            } else {
//                stdoutBuffer.setLength(0);
                ProcessBuilder pb = new ProcessBuilder(siadFile.getAbsolutePath(), "-M", "gctw");
                pb.redirectErrorStream(true);
                pb.directory(Utils.INSTANCE.getWorkingDirectory(Siad.this));
                try {
                    siadProcess = pb.start();
                    readStdoutThread = new Thread(() -> {
                        try {
                            BufferedReader inputReader = new BufferedReader(new InputStreamReader(siadProcess.getInputStream()));
                            String line;
                            while ((line = inputReader.readLine()) != null) {
                                siadNotification(line);
                                if (line.contains("Finished loading") || line.contains("Done!"))
                                    WalletMonitorService.Companion.staticRefresh();
                            }
                            inputReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    readStdoutThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    siadNotification("Failed to start");
                }
            }
        });
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
//        Daemon.stopSpecific("localhost:9980", new SiaRequest.VolleyCallback(null));
        if (siadProcess != null)
            siadProcess.destroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void siadNotification(String text) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(SIAD_NOTIFICATION, buildSiadNotification(text));
    }

    private Notification buildSiadNotification(String text) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_dns_white_48dp);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sia_logo_transparent);
        builder.setLargeIcon(largeIcon);
        builder.setContentTitle("Local full node");
        builder.setContentText(text);
        builder.setOngoing(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId("sia");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        return builder.build();
    }
}
