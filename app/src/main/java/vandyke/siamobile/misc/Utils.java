/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.misc;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.SiaMobileApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

public class Utils {
    public static final String[] devAddresses = {
            "20c9ed0d1c70ab0d6f694b7795bae2190db6b31d97bc2fba8067a336ffef37aacbc0c826e5d3",
            "36ab7ac91b981f998a0f5417b7f64299375cc5ffe096841044597b48346936b49741bfeb6cf5",
            "65cc0ab13a1ccb7788cf36554daf980f162c5bf2fec9a3664192916b26c568af4eda38f666d0",
            "870878df29ee72082673ddf1e53f5ed2f52a8e84486d85e241ee531c1350066ad9622ed0ec61",
            "8a9d8e6c8d043300b967443eaaa01874efa36e69a95b03c6b970bfe5b82a7f0345424a7919af",
            "986082d52bf8a25009e7ce97508385687f3241d1e027969edbf9f63e4240cecf77bad58f40a5",
            "a58c0c63ec11b8b7b6410e76920882ea312a6762c2da98e0376ee96a8e23392f9df4e403c584",
            "a61260748a55cdbed8c28038724ada4c5284062ae799df530147aa9b8809c929145585a83cdb",
            "b05b1603c8e640a6617107d3f8f90925c13d98213822afee0c481022ef236ee9bae778ea2971",
            "ca4e94a53e257fcac10d8890aa76d73bf6a6490686301232236f8d99c4dedc1158857cf6c558",
            "f39caefc5e7f5f92a3e13a04837524a8096bc3873f551e3bd1f6c6c4cff2d2c664ddf6cfa27f"};
    public static final BigDecimal devFee = new BigDecimal("0.005"); // 0.5%

    public static AlertDialog.Builder getDialogBuilder(Context context) {
        switch (MainActivity.theme) {
            case LIGHT:
            case DARK:
                return new AlertDialog.Builder(context);
            case AMOLED:
                return new AlertDialog.Builder(context, R.style.DialogTheme_Amoled);
            case CUSTOM:
                return new AlertDialog.Builder(context, R.style.DialogTheme_Custom);
            default:
                return new AlertDialog.Builder(context);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity == null)
            return;
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void snackbar(View view, String text, int duration) {
        if (view == null || !view.isShown())
            return;
        Snackbar snackbar = Snackbar.make(view, text, duration);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorAccent));
        snackbar.show();
    }

    public static void successSnackbar(View view) {
        snackbar(view, "Success", Snackbar.LENGTH_SHORT);
    }

    public static boolean isSiadSupported() {
        return SiaMobileApplication.abi.equals("arm64");
    }

    // will return null if the abi is an unsupported one and therefore there is not a binary for it
    public static File copyBinary(String filename, Context context, boolean bit32) {
        try {
            InputStream in;
            File result;
            if (bit32) {
                in = context.getAssets().open(filename + "-" + SiaMobileApplication.abi32);
                result = new File(context.getFilesDir(), filename + "-" + SiaMobileApplication.abi32);
            } else {
                in = context.getAssets().open(filename + "-" + SiaMobileApplication.abi);
                result = new File(context.getFilesDir(), filename + "-" + SiaMobileApplication.abi);
            }
            if (result.exists())
                return result;
            FileOutputStream out = new FileOutputStream(result);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
            result.setExecutable(true);
            in.close();
            out.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getWorkingDirectory(Context context) {
        if (context == null)
            return null;
        File result;
        if (SiaMobileApplication.prefs.getBoolean("useExternal", false)) {
            result = context.getExternalFilesDir(null);
            if (result == null) { // external storage not found
                Toast.makeText(context, "No external storage found. Using internal", Toast.LENGTH_LONG).show();
                result = context.getFilesDir();
            }
        } else
            result = context.getFilesDir();
        return result;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static String externalStorageStateDescription() {
        switch (Environment.getExternalStorageState()) {
            case Environment.MEDIA_BAD_REMOVAL:
                return "external storage was previously removed before being unmounted";
            case Environment.MEDIA_CHECKING:
                return "external storage is present but being disk-checked";
            case Environment.MEDIA_EJECTING:
                return "external storage is in the process of ejecting";
            case Environment.MEDIA_MOUNTED:
                return "external storage is present and mounted with read/write access";
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                return "external storage is present but mounted as read-only";
            case Environment.MEDIA_NOFS:
                return "external storage is present but is blank or using an unsupported filesystem";
            case Environment.MEDIA_REMOVED:
                return "external storage is not present";
            case Environment.MEDIA_SHARED:
                return "external storage is present but being shared via USB";
            case Environment.MEDIA_UNKNOWN:
                return "external storage is in an unknown state";
            case Environment.MEDIA_UNMOUNTABLE:
                return "external storage is present but cannot be mounted. May be corrupted";
            case Environment.MEDIA_UNMOUNTED:
                return "external storage is present but unmounted";
            default:
                return "external storage state missed all cases";
        }
    }

    public static void notification(Context context, int id, int icon, String title, String text, boolean ongoing) {
        if (context == null)
            return;
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(icon);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.sia_logo_transparent);
        builder.setLargeIcon(largeIcon);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setOngoing(ongoing);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }

    public static void cancelNotification(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }
}
