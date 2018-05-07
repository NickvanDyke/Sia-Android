package com.vandyke.sia.data.siad

import android.app.*
import android.arch.lifecycle.LifecycleService
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.support.v4.app.NotificationCompat
import android.text.SpannableString
import android.text.style.StyleSpan
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.DownloadData
import com.vandyke.sia.data.models.renter.DownloadData.Status
import com.vandyke.sia.data.models.renter.name
import com.vandyke.sia.data.remote.SiaApi
import com.vandyke.sia.data.siad.SiadStatus.State.*
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.util.bitmapFromVector
import com.vandyke.sia.util.pluralize
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

    private val inProgressDls = mutableListOf<DownloadData>()
    /** holds dls that were in siad's download queue and completed upon starting the service, so that we don't track them */
    private val dontTrack = mutableListOf<DownloadData>()
    /** completed downloads (both success + error), so that we can continue including them in summary notifications,
     * and only show an individual notification for them once */
    private val completedDls = mutableListOf<DownloadData>()
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
        if (siadStatus.state.value == SIAD_LOADED)
            loopUpdate()
        siadStatus.stateEvent.observe(this) {
            when (it) {
                SIAD_LOADED -> loopUpdate()
                CRASHED, MANUALLY_STOPPED, RESTARTING, SERVICE_STOPPED, UNMET_CONDITIONS -> onSiadStopped()
            }
        }
    }

    private fun loopSum() {
        summaryNotification()
        handler.postDelayed(::loopSum, 2000)
    }

    private fun update() {
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
                                if (download !in dontTrack && download !in completedDls) {
                                    if (download !in inProgressDls) {
                                        addTrackedDownload(download)
                                    } else {
                                        updateTrackedDownload(download)
                                    }
                                }
                            }

                    summaryNotification()

                    if (downloads.all(DownloadData::completed)) {
                        stopSelf()
                        return@subscribe
                    }
                }, {}) // is there some action that should be taken on errors? Maybe show an error notification?
    }

    private fun loopUpdate() {
        update()
        handler.postDelayed(::loopUpdate, 2000)
    }

    private fun addTrackedDownload(download: DownloadData) {
        inProgressDls.add(download)
        downloadNotification(download)
    }

    private fun updateTrackedDownload(download: DownloadData) {
        val index = inProgressDls.indexOf(download)
        inProgressDls[index] = download
        downloadNotification(download)
        if (download.completed) {
            completedDls.add(download)
            inProgressDls.removeAt(index)
        }
    }

    private fun downloadNotification(download: DownloadData) {
        builder.setContentTitle(download.destination.name())
                .setGroupSummary(false)
                .setStyle(null)
                .mActions.clear()

        when (download.status) {
            Status.COMPLETED_SUCCESSFULLY -> {
                builder.setContentText("Download complete.") // maybe show size downloaded?
                        .setLargeIcon(bitmapFromVector(R.drawable.ic_check_circle_green))
                        .setContentIntent(PendingIntent.getActivity(this, 0, Intent(DownloadManager.ACTION_VIEW_DOWNLOADS), PendingIntent.FLAG_UPDATE_CURRENT))
                        .setAutoCancel(true)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
            }

            Status.IN_PROGRESS -> {
                builder.setContentText("${download.progress}%") // TODO: maybe include received/length
                        .setLargeIcon(bitmapFromVector(R.drawable.ic_cloud_download_siagreen))
                        .setContentIntent(null)
                        .setAutoCancel(false)
                        .setProgress(100, download.progress, false)
                        .setOngoing(true)
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
                val pi = PendingIntent.getBroadcast(this, download.destination.hashCode(), intent, 0)
                builder.addAction(R.drawable.ic_refresh_white, "Retry", pi)
            }
        }

        /* we can use destination.hashCode() as the ID because FilesRepository
         * ensures that files being downloaded will have unique destinations */
        builder.show(this, download.destination.hashCode())
    }

    // TODO: whenever there are two download notifications showing, the summary notification
    // flickers when it's refreshed. Starting with one and going to two makes it happen.
    // Going to three from two makes it stop. Using a separate builder didn't fix it.
    // It also doesn't flicker when expanded into the individual notifications.
    private fun summaryNotification() {
        val dls = inProgressDls + completedDls
        if (dls.isEmpty())
            return
        val numInProgress = inProgressDls.size
        val progress = dls.sumBy(DownloadData::progress) / dls.size
        val numErrors = completedDls.count { it.error.isNotEmpty() }

        val style = NotificationCompat.InboxStyle()
        /* add lines for each download */
        dls.forEach { download ->
            val str = SpannableString("${download.destination.name()} " + when (download.status) {
                Status.COMPLETED_SUCCESSFULLY -> "Completed"
                Status.IN_PROGRESS -> "${download.progress}%"
                Status.ERROR_OCCURRED -> "Error: ${download.error}"
            })
            str.setSpan(StyleSpan(Typeface.BOLD), 0, download.destination.name().length, 0)
            style.addLine(str)
        }

        builder.setGroupSummary(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, Intent(DownloadManager.ACTION_VIEW_DOWNLOADS), PendingIntent.FLAG_UPDATE_CURRENT))
                .mActions.clear()

        if (numInProgress > 0) {
            val title = "Downloading $numInProgress ${"file".pluralize(numInProgress)}${if (numErrors > 0) " ($numErrors ${"error".pluralize(numErrors)})" else ""}"
            style.setBigContentTitle(title)
            builder.setContentTitle(title)
                    .setContentText("$progress%")
                    .setProgress(100, progress, false)
                    .setOngoing(true)
                    .setLargeIcon(bitmapFromVector(R.drawable.ic_cloud_download_siagreen))
        } else {
            val title = "${dls.size} downloads completed"
            style.setBigContentTitle("$title ($numErrors ${"error".pluralize(numErrors)})")
            builder.setContentTitle(title)
                    .setContentText("$numErrors ${"error".pluralize(numErrors)}")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
            if (numErrors > 0) {
                builder.setLargeIcon(bitmapFromVector(R.drawable.ic_error_red))
            } else {
                builder.setLargeIcon(bitmapFromVector(R.drawable.ic_check_circle_green))
            }
        }

        builder.setStyle(style)
        builder.show(this, SUMMARY_ID)
    }

    private fun onSiadStopped() {
        /* cancel the repeating updates. When/if siad is loaded again, update will begin due to observing its state */
        handler.removeCallbacksAndMessages(null)

        /* siad does not support resuming downloads, so we mark all in-progress downloads as failed. */
        inProgressDls.filter { it.status == Status.IN_PROGRESS }
                .forEach { updateTrackedDownload(it.copy(completed = true, error = "Sia node stopped")) }

        if (inProgressDls.isNotEmpty() || completedDls.isNotEmpty())
            summaryNotification()

        /* we then clear the tracked downloads, because when siad is started again, its download queue will be empty */
        inProgressDls.clear()
        completedDls.clear()
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
        const val GROUP_KEY = "DOWNLOADS"
        const val DOWNLOADS_CHANNEL = "DOWNLOADS_CHANNEL"

    }
}