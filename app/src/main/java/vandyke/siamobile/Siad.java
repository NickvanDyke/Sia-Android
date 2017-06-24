package vandyke.siamobile;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import vandyke.siamobile.api.Daemon;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.fragments.TerminalFragment;
import vandyke.siamobile.fragments.WalletFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Siad {

    public static int SIAD_NOTIFICATION = 1;

    private static Siad instance;
    private File siadFile;
    private Process siadProcess;
    private Thread readStdoutThread;
    final private StringBuilder stdoutBuffer = new StringBuilder();
    private static TerminalFragment terminalFragment;
    private boolean finishedLoading;

    private Siad() {
        siadFile = MainActivity.copyBinary("siad");
        instance = this;
        finishedLoading = false;
    }

    public static Siad getInstance() {
        if (instance == null)
            instance = new Siad();
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void start() {
        if (siadFile == null) {
            terminalAppend("Your device's processor architecture is not supported by siad. Sorry! There's nothing Sia Mobile can do about this");
            return;
        }
        if (siadProcess != null) {
            System.out.println("siad already running");
            return;
        }
        stdoutBuffer.setLength(0);
        terminalAppend("\nStarting siad...\n");
        ProcessBuilder pb = new ProcessBuilder(siadFile.getAbsolutePath(), "-M", "gctw");
        pb.redirectErrorStream(true);
        pb.directory(MainActivity.getWorkingDirectory());
        try {
            siadProcess = pb.start();
            System.out.println(siadProcess);
            readStdoutThread = new Thread() {
                public void run() {
                    try {
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(siadProcess.getInputStream()));
                        int read;
                        char[] buffer = new char[1024];
                        while ((read = inputReader.read(buffer)) > 0) {
                            final String text = new String(buffer).substring(0, read);
                            if (text.contains("Finished loading"))
                                WalletFragment.instance.refresh();
                            siadNotification(text);
                            System.out.println(text);
                            MainActivity.instance.runOnUiThread(new Runnable() {
                                public void run() {
                                    terminalAppend(text);
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
        Daemon.stop(new SiaRequest.VolleyCallback());
        siadProcess = null;
        terminalAppend("Stopping siad... (may take a while. It's okay to close Sia Mobile during this)\n");
    }

    public void forceStop() {
        if (siadProcess != null) {
            siadProcess.destroy();
            siadProcess = null;
        }
        destroyInstance();
        terminalAppend("Force stopped siad\n");
    }

    public String getBufferedStdout() {
        if (stdoutBuffer == null)
            return "";
        String result = stdoutBuffer.toString();
        stdoutBuffer.setLength(0);
        return result;
    }

    public static void setTerminalFragment(TerminalFragment fragment) {
        terminalFragment = fragment;
    }

    private void terminalAppend(String text) {
        if (terminalFragment != null)
            terminalFragment.appendToOutput(text);
        else
            stdoutBuffer.append(text);
    }

    private void siadNotification(String text) {
        Notification.Builder builder = new Notification.Builder(MainActivity.instance);
        builder.setSmallIcon(R.drawable.ic_sync_white_48dp);
        Bitmap largeIcon = BitmapFactory.decodeResource(MainActivity.instance.getResources(), R.drawable.sia_logo_transparent);
        builder.setLargeIcon(largeIcon);
        builder.setContentTitle("Sia Mobile siad");
        builder.setContentText(text);
        builder.setOngoing(false);
        Intent intent = new Intent(MainActivity.instance, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.instance, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)MainActivity.instance.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(SIAD_NOTIFICATION, builder.build());
    }
}
