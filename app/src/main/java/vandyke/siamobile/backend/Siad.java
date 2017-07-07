/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.Daemon;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.terminal.fragments.TerminalFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Siad extends Service {

    public static int SIAD_NOTIFICATION = 1;

    private File siadFile;
    private java.lang.Process siadProcess;
    private Thread readStdoutThread;
    final private StringBuilder stdoutBuffer = new StringBuilder();
    private TerminalFragment terminalFragment;

    public void start(final Activity activity) {
        if (siadFile == null) {
            terminalAppend("Your device's processor architecture is not supported by Sia's full node. Sorry! There's nothing Sia Mobile can do about this");
            return;
        }
        if (siadProcess != null) {
            return;
        }
        stdoutBuffer.setLength(0);
        terminalAppend("\nStarting siad...\n");
        ProcessBuilder pb = new ProcessBuilder(siadFile.getAbsolutePath(), "-M", "gctw");
        pb.redirectErrorStream(true);
        pb.directory(MainActivity.getWorkingDirectory(activity));
        try {
            siadProcess = pb.start();
            readStdoutThread = new Thread() {
                public void run() {
                    try {
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(siadProcess.getInputStream()));
                        String line;
                        while ((line = inputReader.readLine()) != null) {
//                            if (line.contains("Finished loading") || line.contains("Done!"))
//                                WalletFragment.refreshWallet(activity.getFragmentManager());
//                            siadNotification(line, activity);
                            final String lineFinal = line + "\n";
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    terminalAppend(lineFinal);
                                }
                            });
                        }
                        inputReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            readStdoutThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        Daemon.stop(new SiaRequest.VolleyCallback(null));
        siadProcess = null;
        terminalAppend("Stopping siad... (may take a while. It's okay to close Sia Mobile during this)\n");
    }

    public String getBufferedStdout() {
        if (stdoutBuffer == null)
            return "";
        String result = stdoutBuffer.toString();
        stdoutBuffer.setLength(0);
        return result;
    }

    public void setTerminalFragment(TerminalFragment fragment) {
        terminalFragment = fragment;
    }

    private void terminalAppend(String text) {
        if (terminalFragment != null)
            terminalFragment.appendToOutput(text);
        else
            stdoutBuffer.append(text);
    }

    private void siadNotification(String text) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_sync_white_48dp);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sia_logo_transparent);
        builder.setLargeIcon(largeIcon);
        builder.setContentTitle("Sia Mobile siad");
        builder.setContentText(text);
        builder.setOngoing(false);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(SIAD_NOTIFICATION, builder.build());
    }

    @Override
    public void onCreate() {
        final Siad instance = this;
        Thread thread = new Thread() {
            public void run() {
                System.out.println("onCreate");
                siadFile = MainActivity.copyBinary("siad", instance, false);
                if (siadFile == null) {
                    siadNotification("Your Android device has an unsupported CPU architecture");
                    stopSelf();
                }
                stdoutBuffer.setLength(0);
                ProcessBuilder pb = new ProcessBuilder(siadFile.getAbsolutePath(), "-M", "gctw");
                pb.redirectErrorStream(true);
                pb.directory(MainActivity.getWorkingDirectory(instance));
                try {
                    siadProcess = pb.start();
                    readStdoutThread = new Thread() {
                        public void run() {
                            try {
                                BufferedReader inputReader = new BufferedReader(new InputStreamReader(siadProcess.getInputStream()));
                                String line;
                                while ((line = inputReader.readLine()) != null) {
                                    siadNotification(line);
                                }
                                inputReader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    readStdoutThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand");
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        System.out.println("Siad service destroyed");
    }
}
