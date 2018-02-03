/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.siad

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.vandyke.sia.appComponent
import javax.inject.Inject


class SiadReceiver : BroadcastReceiver() {

    @Inject lateinit var siadSource: SiadSource

    init {
        appComponent.inject(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ConnectivityManager.CONNECTIVITY_ACTION -> {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = cm.activeNetworkInfo
                siadSource.activeNetworkType = activeNetwork?.type
            }
        }
    }
}