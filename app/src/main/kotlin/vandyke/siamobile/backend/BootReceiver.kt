/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import vandyke.siamobile.backend.coldstorage.ColdStorageService
import vandyke.siamobile.backend.siad.SiadMonitorService
import vandyke.siamobile.backend.wallet.WalletService
import vandyke.siamobile.prefs

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (prefs.runInBackground) {
            when (prefs.operationMode) {
                "cold_storage" -> context.startService(Intent(context, ColdStorageService::class.java))
                "local_full_node" -> context.startService(Intent(context, SiadMonitorService::class.java))
            }
            context.startService(Intent(context, WalletService::class.java))
        }
    }
}
