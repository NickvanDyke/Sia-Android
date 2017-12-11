/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.siad

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import vandyke.siamobile.R
import vandyke.siamobile.ui.MainActivity
import vandyke.siamobile.ui.settings.Prefs
import vandyke.siamobile.util.NotificationUtil
import vandyke.siamobile.util.StorageUtil
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class SiadService : Service() {

    private val binder = LocalBinder()
    private val statusReceiver: StatusReceiver = StatusReceiver(this)
    private var siadFile: File? = null
    private var siadProcess: java.lang.Process? = null
    lateinit var wakeLock: PowerManager.WakeLock
    private val SIAD_NOTIFICATION = 3
    var isSiadRunning: Boolean = false
        get() = siadProcess != null

    override fun onCreate() {
        startForeground(SIAD_NOTIFICATION, buildSiadNotification("Starting service..."))
//        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
//        applicationContext.registerReceiver(statusReceiver, intentFilter)
        siadFile = StorageUtil.copyBinary("siad", this)
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sia node")
        startSiad()
    }

    /**
     * should only be called from the SiadService's BroadcastReceiver or from onCreate of this service
     */
    fun startSiad() {
        if (siadProcess != null) {
            return
        }
        if (siadFile == null) {
            siadNotification("Couldn't start Siad")
            return
        }
        siadIsLoaded.onNext(false)
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
                    output.onNext(line)
                    line = inputReader.readLine()
                }
                inputReader.close()
                output.onComplete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        output.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.contains("Finished loading"))
                        siadIsLoaded.onNext(true)
                    siadNotification(it)
                }
    }

    /**
     * should only be called from the SiadService's BroadcastReceiver or from onDestroy of this service
     */
    fun stopSiad() {
        siadIsLoaded.onNext(false)
        if (wakeLock.isHeld)
            wakeLock.release()
        // TODO: maybe shut it down using stop http request instead? Takes ages sometimes. But might fix the (sometime) long startup times
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
        stopForeground(true)
        stopSiad()
    }

    fun siadNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SIAD_NOTIFICATION, buildSiadNotification(text))
    }

    private fun buildSiadNotification(text: String): Notification {
        val builder = NotificationCompat.Builder(this, NotificationUtil.NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_local_full_node)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.sia_logo_transparent))
                .setContentTitle("Sia node")
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        return builder.build()
    }

    companion object {
        /**
         * HAVE to unsubscribe from this properly, or else crashes could occur when the app is killed and
         * later restarted if the static variable hasn't been cleared, and therefore isn't recreated, and
         * will still have references to old, now non-existent subscribers.
         * Primary reason for using a static variable for it is because that way it exists independently of
         * the service, and is not destroyed when the service is, meaning that subscribers will still receive updates
         * when the service is restarted and causes the observable to emit.
         */
        val output = PublishSubject.create<String>()!!

        val siadIsLoaded = BehaviorSubject.create<Boolean>()!!

        fun isBatteryGood(intent: Intent): Boolean {
            if (intent.action != Intent.ACTION_BATTERY_CHANGED)
                return false
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
            return (level * 100 / scale) >= Prefs.localNodeMinBattery
        }

        fun isConnectionGood(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetInfo = connectivityManager.activeNetworkInfo
            return activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI || Prefs.runLocalNodeOffWifi
            // TODO: maybe this should instead check that the type is not TYPE_DATA? Depends on the behavior I want
        }

        fun singleAction(context: Context, action: (service: SiadService) -> Unit) {
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    action((service as SiadService.LocalBinder).service)
                    context.unbindService(this)
                }

                override fun onServiceDisconnected(name: ComponentName) {}
            }
            context.bindService(Intent(context, SiadService::class.java), connection, Context.BIND_AUTO_CREATE)
        }

        fun getService(context: Context) =
                Single.create<SiadService> {
                    val connection = object : ServiceConnection {
                        override fun onServiceConnected(name: ComponentName, service: IBinder) {
                            it.onSuccess((service as SiadService.LocalBinder).service)
                            context.unbindService(this)
                        }

                        override fun onServiceDisconnected(name: ComponentName) {}
                    }
                    context.bindService(Intent(context, SiadService::class.java), connection, Context.BIND_AUTO_CREATE)
                }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        val service: SiadService
            get() = this@SiadService
    }
}
