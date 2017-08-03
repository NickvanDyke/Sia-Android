/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.siad

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import vandyke.siamobile.MainActivity
import vandyke.siamobile.R
import vandyke.siamobile.backend.wallet.WalletService
import vandyke.siamobile.util.NotificationUtil
import vandyke.siamobile.util.StorageUtil
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class Siad : Service() {

    private val SIAD_NOTIFICATION = 3

    private var siadFile: File? = null
    private var siadProcess: java.lang.Process? = null

    override fun onCreate() {
        startForeground(SIAD_NOTIFICATION, buildSiadNotification("Starting..."))
        val thread = Thread {
            siadFile = StorageUtil.copyBinary("siad", this@Siad, false)
            if (siadFile == null) {
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    NotificationUtil.notification(this@Siad, SIAD_NOTIFICATION, R.drawable.ic_dns,
                            "Local full node", "Unsupported CPU architecture", false)
                }
                stopForeground(true)
                stopSelf()
            } else {
                val pb = ProcessBuilder(siadFile?.absolutePath, "-M", "gctw")
                pb.redirectErrorStream(true)
                pb.directory(StorageUtil.getWorkingDirectory(this@Siad))
                try {
                    siadProcess = pb.start()
                    Thread {
                        try {
                            val inputReader = BufferedReader(InputStreamReader(siadProcess?.inputStream))
                            var line: String? = inputReader.readLine()
                            while (line != null) {
                                siadNotification(line)
                                if (line.contains("Finished loading") || line.contains("Done!"))
                                    WalletService.singleAction(applicationContext, { it.refresh() })
                                line = inputReader.readLine()
                            }
                            inputReader.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                    siadNotification("Failed to start")
                }

            }
        }
        thread.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onDestroy() {
        //        Daemon.stopSpecific("localhost:9980", new SiaRequest.VolleyCallback(null));
        siadProcess?.destroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun siadNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SIAD_NOTIFICATION, buildSiadNotification(text))
    }

    private fun buildSiadNotification(text: String): Notification {
        val builder = Notification.Builder(this)
        builder.setSmallIcon(R.drawable.ic_dns)
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
}
