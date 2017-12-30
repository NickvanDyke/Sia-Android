/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.siad

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import com.vandyke.siamobile.R
import com.vandyke.siamobile.data.local.Prefs
import com.vandyke.siamobile.isSiadLoaded
import com.vandyke.siamobile.siadOutput
import com.vandyke.siamobile.ui.main.MainActivity
import com.vandyke.siamobile.util.NotificationUtil
import com.vandyke.siamobile.util.StorageUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class SiadService : Service() {

    private var siadFile: File? = null
    private var siadProcess: java.lang.Process? = null
    lateinit var wakeLock: PowerManager.WakeLock
    private val SIAD_NOTIFICATION = 3
    val siadProcessIsRunning: Boolean
        get() = siadProcess != null
    private var subscription: Disposable? = null
    private val receiver = SiadReceiver()

    override fun onCreate() {
        val filter = IntentFilter(SiadReceiver.STOP_SERVICE)
        filter.addAction(SiadReceiver.START_SIAD)
        filter.addAction(SiadReceiver.STOP_SIAD)
        registerReceiver(receiver, filter)
        // TODO: need some way to do this such that if I push an update with a new version of siad, that it will overwrite the
        // current one. Maybe just keep the version in sharedprefs and check against it?
        siadFile = StorageUtil.copyFromAssetsToAppStorage("siad", this)
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sia node")
        if (Prefs.startSiaAutomatically)
            startSiad()
    }

    fun startSiad() {
        if (siadProcessIsRunning) {
            return
        }
        if (siadFile == null) {
            showSiadNotification("Couldn't start Siad")
            return
        }
        isSiadLoaded.onNext(false)

        /* acquire partial wake lock to keep device CPU awake and therefore keep the Sia node active */
        if (Prefs.SiaNodeWakeLock) {
            wakeLock.acquire()
        }
        val pb = ProcessBuilder(siadFile!!.absolutePath, "-M", "gctw") // TODO: maybe let user set which modules to load?
        pb.redirectErrorStream(true)

        /* start the node with an api password if it's not set to something empty */
        if (Prefs.apiPassword.isNotEmpty()) {
            val args = pb.command()
            args.add("--authenticate-api")
            pb.environment().put("SIA_API_PASSWORD", Prefs.apiPassword)
            pb.command(args)
        }

        /* determine what directory Sia should use. Display notification with errors if external storage is set and not working */
        if (Prefs.useExternal) {
            if (StorageUtil.isExternalStorageWritable) {
                val dir = getExternalFilesDir(null)
                if (dir != null) {
                    pb.directory(dir)
                } else {
                    showSiadNotification("Error getting external storage")
                    return
                }
            } else {
                showSiadNotification(StorageUtil.externalStorageStateDescription())
                return
            }
        } else {
            pb.directory(filesDir)
        }

        try {
            siadProcess = pb.start() // TODO: this causes the application to skip about a second of frames when starting. Preventable?
            startForeground(SIAD_NOTIFICATION, siadNotification("Starting Sia node..."))
            // should this be done directly in the spot that reads from output instead? Don't think it matters
            subscription = siadOutput.observeOn(AndroidSchedulers.mainThread()).subscribe {
                if (it.contains("Finished loading"))
                    isSiadLoaded.onNext(true)
                showSiadNotification(it)
            }
            launch(CommonPool) {
                /* need another try-catch block since this is inside a coroutine */
                try {
                    val inputReader = BufferedReader(InputStreamReader(siadProcess!!.inputStream))
                    var line: String? = inputReader.readLine()
                    while (line != null) {
                        siadOutput.onNext(line)
                        line = inputReader.readLine()
                    }
                    inputReader.close()
                    siadOutput.onComplete()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: IOException) {
            showSiadNotification(e.localizedMessage ?: "Error starting Sia node")
        }
    }

    fun stopSiad() {
        if (wakeLock.isHeld)
            wakeLock.release()
        // TODO: maybe shut it down using stop http request instead? Takes ages sometimes
        isSiadLoaded.onNext(false)
        subscription?.dispose()
        siadProcess?.destroy()
        siadProcess = null
        stopForeground(false)
        showSiadNotification("Stopped")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        stopSiad()
        stopForeground(true)
        NotificationUtil.cancelNotification(this, SIAD_NOTIFICATION)
        if (wakeLock.isHeld)
            wakeLock.release()
    }

    fun showSiadNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SIAD_NOTIFICATION, siadNotification(text))
    }

    private fun siadNotification(text: String): Notification {
        val builder = NotificationCompat.Builder(this, NotificationUtil.NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.siacoin_logo_svg_white)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.sia_logo_transparent))
                .setContentTitle("Sia node")
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))

        /* the intent that launches MainActivity when the notification is selected */
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPI = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(contentPI)

        /* the action to stop/start the Sia node */
        if (siadProcessIsRunning) {
            val stopIntent = Intent(SiadReceiver.STOP_SIAD)
            stopIntent.setClass(this, SiadReceiver::class.java)
            val stopPI = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            builder.addAction(R.drawable.siacoin_logo_svg_white, "Stop", stopPI)
        } else {
            val startIntent = Intent(SiadReceiver.START_SIAD)
            startIntent.setClass(this, SiadReceiver::class.java)
            val startPI = PendingIntent.getBroadcast(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            builder.addAction(R.drawable.siacoin_logo_svg_white, "Start", startPI)
        }

        /* swiping the notification stops the service - only want this when the Sia node isn't running, and the
         * service has been set to not be foreground */
        if (!siadProcessIsRunning) {
            val stopIntent = Intent(SiadReceiver.STOP_SERVICE)
            stopIntent.setClass(this, SiadReceiver::class.java)
            val stopPI = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            builder.setDeleteIntent(stopPI)
        }

        return builder.build()
    }

    companion object {
        // TODO: maybe emit an error from this if the service isn't already running, and don't attempt to bind?
        // If I don't want to start it as a result of binding, that is. Not sure if that's what I'll want
        fun getService(context: Context) = Single.create<SiadService> {
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    it.onSuccess((service as SiadService.LocalBinder).service)
                    context.unbindService(this)
                }

                override fun onServiceDisconnected(name: ComponentName) {}
            }
            context.bindService(Intent(context, SiadService::class.java), connection, Context.BIND_AUTO_CREATE)
        }!!
    }

    /* binding stuff */
    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        val service: SiadService
            get() = this@SiadService
    }
}
