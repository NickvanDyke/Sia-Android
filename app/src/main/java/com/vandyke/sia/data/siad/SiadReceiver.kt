/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.siad

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.util.io
import com.vandyke.sia.util.main


class SiadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            START_SIAD -> startSiad(context)

            STOP_SIAD -> {
                stopSiad(context)
                Prefs.siaStoppedManually = true
            }

            ConnectivityManager.CONNECTIVITY_ACTION -> {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = cm.activeNetworkInfo
                if (activeNetwork != null && activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                    if (Prefs.runSiaOnData) {
                        startSiad(context)
                    } else {
                        stopSiad(context)
                    }
                } else if (!Prefs.siaStoppedManually) {
                    startSiad(context)
                }
            }
        }
    }

    fun startSiad(context: Context) {
        SiadService.getService(context.applicationContext)
                .io()
                .main()
                .subscribe { service ->
                    service.startSiad()
                }
    }

    fun stopSiad(context: Context) {
        SiadService.getService(context.applicationContext)
                .io()
                .main()
                .subscribe { service ->
                    service.stopSiad()
                }
    }

    companion object {
        /* intent actions meant for this receiver */
        val STOP_SIAD = "STOP_SIAD"
        val START_SIAD = "START_SIAD"
    }
}