package com.vandyke.sia.data.siad

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vandyke.sia.data.remote.SiaApi
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.util.rx.io
import com.vandyke.sia.util.rx.main
import javax.inject.Inject

class RetryDownloadReceiver : BroadcastReceiver() {
    @Inject
    lateinit var api: SiaApi

    override fun onReceive(context: Context, intent: Intent) {
        context.getAppComponent().inject(this)

        if (intent.action != RETRY_INTENT)
            throw Exception()

        val siapath = intent.getStringExtra(RETRY_INTENT_KEY_SIAPATH) ?: throw Exception()
        val destinaton = intent.getStringExtra(RETRY_INTENT_KEY_DESTINATION) ?: throw Exception()

        api.renterDownloadAsync(siapath, destinaton)
                .io()
                .main()
                .subscribe({
                    context.startService(Intent(context, DownloadMonitorService::class.java))
                }, {}) // TODO: sometimes after failing to retry the download, the downloads summary notification appears and is ongoing
    }

    companion object {
        const val RETRY_INTENT = "com.vandyke.sia.RETRY_DOWNLOAD"
        const val RETRY_INTENT_KEY_SIAPATH = "com.vandyke.sia.RETRY_DOWNLOAD_KEY_SIAPATH"
        const val RETRY_INTENT_KEY_DESTINATION = "com.vandyke.sia.RETRY_DOWNLOAD_KEY_DESTINATION"
    }
}