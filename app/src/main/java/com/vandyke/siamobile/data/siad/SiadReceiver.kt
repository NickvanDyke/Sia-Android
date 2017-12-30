/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.siad

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SiadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        println("received intent: $intent")
        if (intent.action == STOP_SERVICE)
            context.stopService(Intent(context, SiadService::class.java))
        else
            SiadService.getService(context.applicationContext)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { service ->
                        when (intent.action) {
                            START_SIAD -> service.startSiad()
                            STOP_SIAD -> service.stopSiad()
                        }
                    }
    }

    companion object {
        /* intent actions meant for this receiver */
        val STOP_SERVICE = "STOP_SERVICE"
        val STOP_SIAD = "STOP_SIAD"
        val START_SIAD = "START_SIAD"
    }
}