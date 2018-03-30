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
        // TODO: this extra is returning null, no idea why. I've checked it immediately after adding it and it's there
        println(intent.getStringExtra(RETRY_INTENT_KEY_DESTINATION))

        api.renterDownloadAsync(
                intent.getStringExtra(RETRY_INTENT_KEY_SIAPATH) ?: throw Exception(),
                intent.getStringExtra(RETRY_INTENT_KEY_DESTINATION) ?: throw Exception())
                .io()
                .main()
                .subscribe({
                    context.startService(Intent(context, DownloadMonitorService::class.java))
                }, Throwable::printStackTrace) // TODO: if restarting fails, could update the notification that retry was pressed on with the reason why. "Retry failed: $reason"
    }

    companion object {
        const val RETRY_INTENT = "com.vandyke.sia.RETRY_DOWNLOAD"
        const val RETRY_INTENT_KEY_SIAPATH = "com.vandyke.sia.RETRY_DOWNLOAD_KEY_SIAPATH"
        const val RETRY_INTENT_KEY_DESTINATION = "com.vandyke.sia.RETRY_DOWNLOAD_KEY_DESTINATION"
    }
}