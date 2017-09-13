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
import android.content.*
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import vandyke.siamobile.R
import vandyke.siamobile.prefs
import vandyke.siamobile.ui.MainActivity
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
    private val SIAD_NOTIFICATION = 3
    var isSiadRunning: Boolean = false
        get() = siadProcess != null

    override fun onCreate() {
        startForeground(SIAD_NOTIFICATION, buildSiadNotification("Starting service..."))
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        applicationContext.registerReceiver(statusReceiver, intentFilter)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(statusReceiver, intentFilter)
        siadFile = StorageUtil.copyBinary("siad", this@SiadService, false)
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
            siadNotification("Unsupported CPU architecture")
            return
        }
        val pb = ProcessBuilder(siadFile?.absolutePath, "-M", "gctw")
        pb.redirectErrorStream(true)
        pb.directory(StorageUtil.getWorkingDirectory(this@SiadService))
        try {
            siadProcess = pb.start()
            async(CommonPool) {
                try {
                    val inputReader = BufferedReader(InputStreamReader(siadProcess?.inputStream))
                    var line: String? = inputReader.readLine()
                    while (line != null) {
                        listeners.forEach { it.onSiadOutput("$line\n") }
                        bufferedOutput += "$line\n"
                        siadNotification(line)
                        line = inputReader.readLine()
                    }
                    inputReader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * should only be called from the SiadService's BroadcastReceiver or from onDestroy of this service
     */
    fun stopSiad() {
        // TODO: maybe shut it down using stop http request instead? Takes ages sometimes. But might fix the (sometime) long startup times
        siadProcess?.destroy()
        siadProcess = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onDestroy() {
        applicationContext.unregisterReceiver(statusReceiver)
        NotificationUtil.cancelNotification(applicationContext, SIAD_NOTIFICATION)
        stopSiad()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    fun siadNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SIAD_NOTIFICATION, buildSiadNotification(text))
    }

    private fun buildSiadNotification(text: String): Notification {
        val builder = Notification.Builder(this)
        builder.setSmallIcon(R.drawable.ic_local_full_node)
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.sia_logo_transparent)
        builder.setLargeIcon(largeIcon)
        builder.setContentTitle("Local full node")
        builder.setContentText(text)
        builder.setOngoing(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId("sia")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        return builder.build()
    }

    interface SiadListener {
        fun onSiadOutput(line: String)
    }

    companion object {
        // TODO: might cause memory leaks? I think it should be fine as long as the listeners properly unregister themselves
        private val listeners = mutableListOf<SiadListener>()
        fun addListener(listener: SiadListener) = listeners.add(listener)
        fun removeListener(listener: SiadListener) = listeners.remove(listener)
        var bufferedOutput: String = ""

        fun isBatteryGood(intent: Intent): Boolean {
            if (intent.action != Intent.ACTION_BATTERY_CHANGED)
                return false
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
            return (level * 100 / scale) >= prefs.localNodeMinBattery
        }

        fun isConnectionGood(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetInfo = connectivityManager.activeNetworkInfo
            return activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI || prefs.runLocalNodeOffWifi
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
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        val service: SiadService
            get() = this@SiadService
    }
}
