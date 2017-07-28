/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.misc

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import vandyke.siamobile.MainActivity
import vandyke.siamobile.R
import vandyke.siamobile.SiaMobileApplication
import vandyke.siamobile.prefs
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigDecimal

object Utils {

    val NOTIFICATION_CHANNEL = "sia"
    val devAddresses = arrayOf("20c9ed0d1c70ab0d6f694b7795bae2190db6b31d97bc2fba8067a336ffef37aacbc0c826e5d3", "36ab7ac91b981f998a0f5417b7f64299375cc5ffe096841044597b48346936b49741bfeb6cf5", "65cc0ab13a1ccb7788cf36554daf980f162c5bf2fec9a3664192916b26c568af4eda38f666d0", "870878df29ee72082673ddf1e53f5ed2f52a8e84486d85e241ee531c1350066ad9622ed0ec61", "8a9d8e6c8d043300b967443eaaa01874efa36e69a95b03c6b970bfe5b82a7f0345424a7919af", "986082d52bf8a25009e7ce97508385687f3241d1e027969edbf9f63e4240cecf77bad58f40a5", "a58c0c63ec11b8b7b6410e76920882ea312a6762c2da98e0376ee96a8e23392f9df4e403c584", "a61260748a55cdbed8c28038724ada4c5284062ae799df530147aa9b8809c929145585a83cdb", "b05b1603c8e640a6617107d3f8f90925c13d98213822afee0c481022ef236ee9bae778ea2971", "ca4e94a53e257fcac10d8890aa76d73bf6a6490686301232236f8d99c4dedc1158857cf6c558", "f39caefc5e7f5f92a3e13a04837524a8096bc3873f551e3bd1f6c6c4cff2d2c664ddf6cfa27f")
    val devFee = BigDecimal("0.005") // 0.5%

    fun getDialogBuilder(context: Context): AlertDialog.Builder {
        when (MainActivity.appTheme) {
            MainActivity.Theme.LIGHT, MainActivity.Theme.DARK -> return AlertDialog.Builder(context)
            MainActivity.Theme.AMOLED -> return AlertDialog.Builder(context, R.style.DialogTheme_Amoled)
            MainActivity.Theme.CUSTOM -> return AlertDialog.Builder(context, R.style.DialogTheme_Custom)
            else -> return AlertDialog.Builder(context)
        }
    }

    fun hideSoftKeyboard(activity: Activity?) {
        if (activity == null)
            return
        val inputMethodManager = activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus.windowToken, 0)
    }

    fun snackbar(view: View?, text: String, duration: Int) {
        if (view == null || !view.isShown)
            return
        val snackbar = Snackbar.make(view, text, duration)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.colorAccent))
        snackbar.show()
    }

    fun successSnackbar(view: View?) {
        snackbar(view, "Success", Snackbar.LENGTH_SHORT)
    }

    val isSiadSupported: Boolean
        get() = SiaMobileApplication.abi == "arm64"

    // will return null if the abi is an unsupported one and therefore there is not a binary for it
    fun copyBinary(filename: String, context: Context, bit32: Boolean): File? {
        try {
            val `in`: InputStream
            val result: File
            if (bit32) {
                `in` = context.assets.open(filename + "-" + SiaMobileApplication.abi32)
                result = File(context.filesDir, filename + "-" + SiaMobileApplication.abi32)
            } else {
                `in` = context.assets.open(filename + "-" + SiaMobileApplication.abi)
                result = File(context.filesDir, filename + "-" + SiaMobileApplication.abi)
            }
            if (result.exists())
                return result
            val out = FileOutputStream(result)
            val buf = ByteArray(1024)
            var length: Int = `in`.read(buf)
            while (length > 0) {
                out.write(buf, 0, length)
            }
            result.setExecutable(true)
            `in`.close()
            out.close()
            return result
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun getWorkingDirectory(context: Context?): File? {
        if (context == null)
            return null
        var result: File?
        if (prefs.useExternal) {
            result = context.getExternalFilesDir(null)
            if (result == null) { // external storage not found
                Toast.makeText(context, "No external storage found. Using internal", Toast.LENGTH_LONG).show()
                result = context.filesDir
            }
        } else
            result = context.filesDir
        return result
    }

    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    fun externalStorageStateDescription(): String {
        when (Environment.getExternalStorageState()) {
            Environment.MEDIA_BAD_REMOVAL -> return "external storage was previously removed before being unmounted"
            Environment.MEDIA_CHECKING -> return "external storage is present but being disk-checked"
            Environment.MEDIA_EJECTING -> return "external storage is in the process of ejecting"
            Environment.MEDIA_MOUNTED -> return "external storage is present and mounted with read/write access"
            Environment.MEDIA_MOUNTED_READ_ONLY -> return "external storage is present but mounted as read-only"
            Environment.MEDIA_NOFS -> return "external storage is present but is blank or using an unsupported filesystem"
            Environment.MEDIA_REMOVED -> return "external storage is not present"
            Environment.MEDIA_SHARED -> return "external storage is present but being shared via USB"
            Environment.MEDIA_UNKNOWN -> return "external storage is in an unknown state"
            Environment.MEDIA_UNMOUNTABLE -> return "external storage is present but cannot be mounted. May be corrupted"
            Environment.MEDIA_UNMOUNTED -> return "external storage is present but unmounted"
            else -> return "external storage state missed all cases"
        }
    }

    fun notification(context: Context?, id: Int, icon: Int, title: String, text: String, ongoing: Boolean) {
        if (context == null)
            return
        val builder = Notification.Builder(context)
        builder.setSmallIcon(icon)
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.sia_logo_transparent)
        builder.setLargeIcon(largeIcon)
        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setOngoing(ongoing)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId("sia")
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }

    fun cancelNotification(context: Context, id: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)
    }

    fun createSiaNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(NOTIFICATION_CHANNEL, "Sia Mobile", NotificationManager.IMPORTANCE_LOW)
        channel.vibrationPattern = null
        notificationManager.createNotificationChannel(channel)
    }
}
