/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.siad

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
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

    private val binder = LocalBinder()
    private var siadFile: File? = null
    private var siadProcess: java.lang.Process? = null
    lateinit var wakeLock: PowerManager.WakeLock
    private val SIAD_NOTIFICATION = 3
    val isSiadProcessRunning: Boolean
        get() = siadProcess != null
    private var subscription: Disposable? = null

    override fun onCreate() {
        startForeground(SIAD_NOTIFICATION, buildSiadNotification("Starting service..."))
        // TODO: need some way to do this such that if I push an update with a new version of siad, that it will overwrite the
        // current one. Maybe just keep the version in sharedprefs and check against it?
        siadFile = StorageUtil.copyFromAssetsToAppStorage("siad", this)
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sia node")
        startSiad()
    }

    fun startSiad() {
        if (siadProcess != null) {
            return
        }
        if (siadFile == null) {
            siadNotification("Couldn't start Siad")
            return
        }
        isSiadLoaded.onNext(false)
        /* acquire partial wake lock to keep device CPU awake and therefore keep the Sia node active */
        if (Prefs.SiaNodeWakeLock) {
            wakeLock.acquire()
        }
        val pb = ProcessBuilder(siadFile!!.absolutePath, "-M", "gctw")
        pb.redirectErrorStream(true)
        pb.directory(StorageUtil.getWorkingDirectory(this@SiadService))
        siadProcess = pb.start() // TODO: this causes the application to skip about a second of frames when starting
        launch(CommonPool) {
            try {
                val inputReader = BufferedReader(InputStreamReader(siadProcess!!.inputStream))
                var line: String? = inputReader.readLine()
                while (line != null) {
                    println(line)
                    siadOutput.onNext(line)
                    line = inputReader.readLine()
                }
                inputReader.close()
                siadOutput.onComplete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


        subscription = siadOutput.observeOn(AndroidSchedulers.mainThread()).subscribe {
            if (it.contains("Finished loading"))
                isSiadLoaded.onNext(true)
            siadNotification(it)
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    override fun onDestroy() {
//        applicationContext.unregisterReceiver(statusReceiver)
        stopSiad()
        stopForeground(true)
    }

    fun siadNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SIAD_NOTIFICATION, buildSiadNotification(text))
    }

    private fun buildSiadNotification(text: String): Notification {
        val builder = NotificationCompat.Builder(this, NotificationUtil.NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.siacoin_logo_svg_white)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.sia_logo_transparent))
                .setContentTitle("Sia node")
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))

        /* the intent that launches MainActivity when the notification is selected */
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPI = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(contentPI)

        /* the action that stops this service */

        return builder.build()
    }

    companion object {
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

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        val service: SiadService
            get() = this@SiadService
    }
}
