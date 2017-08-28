/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import vandyke.siamobile.backend.coldstorage.ColdStorageService
import vandyke.siamobile.backend.siad.SiadService
import vandyke.siamobile.backend.wallet.WalletService
import vandyke.siamobile.prefs

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        /* need to check for Android O in some places because Android O will cause a crash when attempting to start a
        * background service when the app is not in foreground */
        if (prefs.runInBackground) {
            when (prefs.operationMode) {
                "cold_storage" -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                        context.startService(Intent(context, ColdStorageService::class.java))
                }
                "local_full_node" -> context.startService(Intent(context, SiadService::class.java))
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                context.startService(Intent(context, WalletService::class.java))
        }
    }
}
