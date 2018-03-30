package com.vandyke.sia.data.siad

import android.app.*
import android.arch.lifecycle.LifecycleService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.support.v4.app.NotificationCompat
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.DownloadData
import com.vandyke.sia.data.models.renter.DownloadData.Status
import com.vandyke.sia.data.models.renter.name
import com.vandyke.sia.data.remote.SiaApi
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.util.bitmapFromVector
import com.vandyke.sia.util.rx.io
import com.vandyke.sia.util.rx.main
import com.vandyke.sia.util.rx.observe
import com.vandyke.sia.util.show
import javax.inject.Inject

/** A service that repeatedly queries siad for downloads, and displays the appropriate notifications.
 * Stops itself when either there are no downloads, or all downloads are complete. So it must be started
 * manually when new downloads are expected to be present, such as right after a call to renterDownloadAsync completes. */
class DownloadMonitorService : LifecycleService() {
    @Inject
    lateinit var api: SiaApi
    @Inject
    lateinit var siadStatus: SiadStatus
    private lateinit var handler: Handler
    private lateinit var builder: NotificationCompat.Builder

    private val trackedDownloads = mutableListOf<DownloadData>()
    /** we only want to display notifications for completed downloads once, so we keep track of ones that have been shown */
    private val dontTrack = mutableListOf<DownloadData>()
    private var firstUpdate = true

    override fun onCreate() {
        super.onCreate()
        getAppComponent().inject(this)

        handler = Handler(mainLooper)
        builder = NotificationCompat.Builder(this, DOWNLOADS_CHANNEL)
                .setGroup(GROUP_KEY)
                .setSmallIcon(R.drawable.sia_new_circle_logo_transparent_white)
        createDownloadsNotificationChannel()

//        addTrackedDownload(DownloadData("", "", 1000, 0, "one", false, "", "", 500, "", 0L))
//        addTrackedDownload(DownloadData("", "", 1000, 0, "two", false, "", "", 500, "", 0L))
//        addTrackedDownload(DownloadData("", "", 1000, 0, "three", false, "", "", 500, "", 0L))
//        addTrackedDownload(DownloadData("", "", 1000, 0, "four", false, "", "", 500, "", 0L))
//        loopSum()
        if (siadStatus.state.value == SiadStatus.State.SIAD_LOADED)
            loopUpdate()
        siadStatus.stateEvent.observe(this) {
            when (it) {
                SiadStatus.State.SIAD_LOADED -> loopUpdate()
                SiadStatus.State.STOPPED -> onSiadStopped()
            }
        }
    }

    private fun loopSum() {
        summaryNotification()
        handler.postDelayed(::loopSum, 2000)
    }

    private fun update() {
        // if I later want to store current downloads in the db, I should be able to pretty easily
        // switch this to subscribe to a flowable from the db that returns a list of all dls in it,
        // and then just update the db. That way, active downloads would be shown on app startup,
        // even when the node hasn't loaded yet. Is that the behavior I want though? Could say that
        // they're paused in the notification until the node loads
        api.renterDownloads()
                .io()
                .main()
                .map { it.downloads ?: listOf() }
                .subscribe({ downloads ->
                    if (downloads.isEmpty()) {
                        stopSelf()
                        return@subscribe
                    }

                    /* the Sia node's download queue will not be cleared until it's restarted. But this service
                     * intermittently starts and stops when there are or aren't active downloads, respectively.
                     * So if we've previously completed all our downloads, and then start more, without
                     * stopping siad in between, then those previously completed downloads will still be in siad's download
                     * queue (and therefore present in this response). So we add all already-completed
                     * downloads to dontTrack if this is our first time querying the active downloads,
                     * so that notifications won't be shown for them. */
                    if (firstUpdate) {
                        dontTrack.addAll(downloads.filter { it.completed })
                        firstUpdate = false
                    }

                    downloads.filter { it.destinationtype.isEmpty() || it.destinationtype == "file" } /* we only want to display notifications for DLs to disk */
                            .forEach { download ->
                                if (download !in dontTrack) {
                                    if (download !in trackedDownloads) {
                                        addTrackedDownload(download)
                                    } else {
                                        updateTrackedDownload(download)
                                    }
                                }
                            }

                    summaryNotification()

                    if (downloads.all { it.completed }) {
                        stopSelf()
                        return@subscribe
                    }
                }, {}) // is there some action that should be taken on errors?
    }

    private fun loopUpdate() {
        update()
        handler.postDelayed(::loopUpdate, 2000)
    }

    private fun addTrackedDownload(download: DownloadData) {
        trackedDownloads.add(download)
        downloadNotification(download)
    }

    private fun updateTrackedDownload(download: DownloadData) {
        val index = trackedDownloads.indexOf(download)
        trackedDownloads[index] = download
        downloadNotification(download)
        if (download.completed) {
            dontTrack.add(download)
            trackedDownloads.removeAt(index)
        }
    }

    private fun downloadNotification(download: DownloadData) {
        builder.setContentTitle(download.destination.name())
                .setGroupSummary(false)

        when (download.status) {
            Status.COMPLETED_SUCCESSFULLY -> {
                builder.setContentText("Download complete.") // maybe show size downloaded?
                        .setLargeIcon(bitmapFromVector(R.drawable.ic_check_circle_green))
                        .setContentIntent(PendingIntent.getActivity(this, 0, Intent(DownloadManager.ACTION_VIEW_DOWNLOADS), PendingIntent.FLAG_UPDATE_CURRENT))
                        .setAutoCancel(true)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .mActions.clear()
            }

            Status.IN_PROGRESS -> {
                builder.setContentText("${download.progress}%")
                        .setLargeIcon(bitmapFromVector(R.drawable.ic_cloud_download_siagreen))
                        .setContentIntent(null)
                        .setAutoCancel(false)
                        .setProgress(100, download.progress, false)
                        .setOngoing(true)
                        .mActions.clear()
            }

            Status.ERROR_OCCURRED -> {
                builder.setContentText("Error: ${download.error}")
                        .setLargeIcon(bitmapFromVector(R.drawable.ic_error_red))
                        .setContentIntent(null)
                        .setAutoCancel(false)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                val intent = Intent(RetryDownloadReceiver.RETRY_INTENT).apply {
                    putExtra(RetryDownloadReceiver.RETRY_INTENT_KEY_SIAPATH, download.siapath)
                    putExtra(RetryDownloadReceiver.RETRY_INTENT_KEY_DESTINATION, download.destination)
                }
                builder.addAction(R.drawable.ic_refresh_white, "Retry", PendingIntent.getBroadcast(this, 0, intent, 0))
            }
        }

        /* we can use destination.hashCode() as the ID because FilesRepository
         * ensures that files being downloaded will have unique destinations */
        builder.show(this, download.destination.hashCode())
    }

    // TODO: whenever there are two download notifications showing, the summary notification
    // flickers when it's refreshed. Starting with one and going to two makes it happen.
    // Going to three from two makes it stop. Using a separate builder didn't fix it.
    private fun summaryNotification() {
        val progress = trackedDownloads.sumBy(DownloadData::progress) / trackedDownloads.size.coerceAtLeast(1)
        builder.setGroupSummary(true)
                .setContentIntent(null)
                .mActions.clear()

        when {
            trackedDownloads.all { it.status == Status.COMPLETED_SUCCESSFULLY } -> {
                builder.setLargeIcon(bitmapFromVector(R.drawable.ic_check_circle_green))
                        .setOngoing(false)
                        .setContentTitle("All downloads complete")
                        .setContentText(null)
                        .setProgress(0, 0, false)
            }
            trackedDownloads.all { it.status == Status.IN_PROGRESS } -> {
                val size = trackedDownloads.filter { it.status == Status.IN_PROGRESS }.size
                builder.setLargeIcon(bitmapFromVector(R.drawable.ic_cloud_download_siagreen))
                        .setOngoing(true)
                        .setContentTitle("Downloading $size ${if (size > 1) "files" else "file"}")
                        .setContentText("$progress%")
                        .setProgress(100, progress, false)
            }
            trackedDownloads.any { it.status == Status.ERROR_OCCURRED } -> {
                builder.setLargeIcon(bitmapFromVector(R.drawable.ic_error_red))
                        .setOngoing(!trackedDownloads.all { it.status == Status.ERROR_OCCURRED })
                        .setContentTitle("Error occurred in all downloads.")
                        .setContentText("$progress%")
                        .setProgress(100, progress, false)
            }
        }

        builder.show(this, SUMMARY_ID)
    }

    private fun onSiadStopped() {
        /* cancel the repeating updates. When/if siad is loaded again, update will begin due to observing its state */
        handler.removeCallbacksAndMessages(null)

        /* if we get this error, then we know siad has either stopped or crashed.
         * siad does not support resuming downloads, so we must mark all in-progress downloads
         * as failed. */
        trackedDownloads.filter { it.status == Status.IN_PROGRESS }
                .forEach { updateTrackedDownload(it.copy(completed = true, error = "Sia node stopped")) }

        if (trackedDownloads.isNotEmpty())
            summaryNotification()

        /* we then clear the tracked downloads, because when siad is started again, it's download queue will be empty */
        trackedDownloads.clear()
        dontTrack.clear()
    }

    private fun createDownloadsNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(DOWNLOADS_CHANNEL, "Downloads", NotificationManager.IMPORTANCE_LOW)
        channel.vibrationPattern = null
        notificationManager.createNotificationChannel(channel)
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
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SUMMARY_ID = 9876
        private const val GROUP_KEY = "DOWNLOADS"
        private const val DOWNLOADS_CHANNEL = "DOWNLOADS_CHANNEL"

    }
}