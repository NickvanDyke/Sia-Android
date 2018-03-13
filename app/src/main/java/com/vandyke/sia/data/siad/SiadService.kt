/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.siad

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.arch.lifecycle.LifecycleService
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadStatus.State
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.ui.main.MainActivity
import com.vandyke.sia.util.NotificationUtil
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.bitmapFromVector
import com.vandyke.sia.util.rx.observe
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject

class SiadService : LifecycleService() {
    @Inject
    lateinit var siadSource: SiadSource
    @Inject
    lateinit var siadStatus: SiadStatus

    private var siadFile: File? = null
    private var siadProcess: java.lang.Process? = null
    private lateinit var handler: Handler

    val siadProcessIsRunning: Boolean
        get() = siadProcess != null

    override fun onCreate() {
        super.onCreate()
        this.getAppComponent().inject(this)

        // TODO: should also check for and delete older versions of Sia
        siadFile = StorageUtil.copyFromAssetsToAppStorage("siad-${Prefs.siaVersion}", this)

        handler = Handler(mainLooper)

        siadSource.onCreate()

        siadSource.allConditionsGood.observe(this) {
            if (it)
                startSiad()
            else
                stopSiad()
        }

        siadSource.restart.observe(this) {
            restartSiad()
        }

        siadStatus.mostRecentSiadOutput.observe(this) {
            showSiadNotification(it)
        }
    }

    private fun startSiad() {
        if (siadProcessIsRunning) {
            return
        } else if (siadFile == null) {
            siadStatus.siadOutput("Couldn't copy siad from assets to app storage")
            return
        }

        siadStatus.state.value = State.PROCESS_STARTING

        val pb = ProcessBuilder(siadFile!!.absolutePath, "-M", Prefs.modulesString) // TODO: maybe let user set which modules to load?
        pb.redirectErrorStream(true)

        /* start the node with an api password if it's not set to something empty */
        if (Prefs.apiPassword.isNotEmpty()) {
            pb.command().add("--authenticate-api")
            pb.environment()["SIA_API_PASSWORD"] = Prefs.apiPassword
        }

        val dir = File(Prefs.siaWorkingDirectory)
        if (!dir.exists()) {
            siadStatus.siadOutput("Error: set working directory doesn't exist")
            return
        } else if (dir.absolutePath != filesDir.absolutePath) {
            if (Environment.getExternalStorageState(dir) != Environment.MEDIA_MOUNTED) {
                siadStatus.siadOutput("Error with external storage: ${Environment.getExternalStorageState(dir)}")
                return
            } else {
                pb.directory(dir)
            }
        } else {
            pb.directory(dir)
        }

        try {
            siadProcess = pb.start() // TODO: this causes the application to skip about a second of frames when starting at the same time as the app. Preventable? Background thread?
            siadStatus.state.value = State.SIAD_LOADING

            startForeground(SIAD_NOTIFICATION, siadNotification("Starting Sia node..."))

            /* launch a coroutine that will read output from the siad process, and update siad observables from it's output */
            launch(CommonPool) {
                /* need another try-catch block since this is inside a coroutine */
                try {
                    val inputReader = BufferedReader(InputStreamReader(siadProcess!!.inputStream))
                    var line: String? = inputReader.readLine()
                    while (line != null) {
                        if (line.contains("Cannot run program")) {
                            siadStatus.siadOutput(line)
                            stopSiad()
                            return@launch
                        }
                        /* seems that sometimes the phone runs a portscan, and siad receives an HTTP request from it, and outputs a weird
                         * error message thingy. It doesn't affect operation at all, and we don't want the user to see it since
                         * it'd just be confusing */
                        if (!line.contains("Unsolicited response received on idle HTTP channel starting with"))
                            siadStatus.siadOutput(line)

                        if (line.contains("Finished loading"))
                            siadStatus.state.postValue(State.SIAD_LOADED)

                        line = inputReader.readLine()
                    }
                    inputReader.close()
                } catch (e: IOException) {
                    Log.d("SiadService", "Sia process reading interrupted")
                }
            }
        } catch (e: IOException) {
            showSiadNotification(e.localizedMessage ?: "Error starting Sia node")
        }
    }

    private fun stopSiad() {
        // TODO: maybe shut it down using http stop request instead? Takes ages sometimes. Might be advantageous though
        siadProcess?.destroy()
        siadProcess = null
        siadStatus.state.value = State.STOPPED
        stopForeground(true)
    }

    /** restarts siad, but only if it's already running */
    private fun restartSiad() {
        if (siadProcessIsRunning) {
            stopSiad()
            /* first remove any pending starts */
            handler.removeCallbacksAndMessages(null)
            /* need to wait a little bit, otherwise siad will report that the address is already in use */
            handler.postDelayed(::startSiad, 1000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        siadSource.onDestroy()
        stopSiad()
        NotificationUtil.cancelNotification(this, SIAD_NOTIFICATION)
    }

    private fun showSiadNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SIAD_NOTIFICATION, siadNotification(text))
    }

    private fun siadNotification(text: String): Notification {
        val builder = NotificationCompat.Builder(this, NotificationUtil.SIA_NODE_CHANNEL)
                .setSmallIcon(R.drawable.sia_new_circle_logo_transparent_white)
                .setLargeIcon(bitmapFromVector(R.drawable.sia_new_circle_logo_transparent))
                .setContentTitle("Sia node")
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))

        /* the intent that launches MainActivity when the notification is selected */
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPI = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(contentPI)

        /* the action to stop/start the Sia node */
        if (siadProcessIsRunning) {
            val stopIntent = Intent(SiadSource.STOP_SIAD)
            val stopPI = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            builder.addAction(R.drawable.sia_new_circle_logo_transparent_white, "Stop", stopPI)
        }

        return builder.build()
    }

    companion object {
        const val SIAD_NOTIFICATION = 3
    }

    /* binding stuff */
    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    inner class LocalBinder : Binder() {
        val service: SiadService
            get() = this@SiadService
    }
}
