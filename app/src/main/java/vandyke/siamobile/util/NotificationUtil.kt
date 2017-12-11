/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import vandyke.siamobile.R
import vandyke.siamobile.ui.main.MainActivity

object NotificationUtil {
    val NOTIFICATION_CHANNEL = "sia"

    fun notification(context: Context, id: Int, icon: Int, title: String, text: String, ongoing: Boolean) {
        val builder = Notification.Builder(context)
        builder.setSmallIcon(icon)
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.sia_logo_transparent)
        builder.setLargeIcon(largeIcon)
        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setOngoing(ongoing)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId(NOTIFICATION_CHANNEL)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }

    fun cancelNotification(context: Context, id: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)
    }

    fun createSiaNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(NOTIFICATION_CHANNEL, "Sia Mobile", NotificationManager.IMPORTANCE_LOW)
        channel.vibrationPattern = null
        notificationManager.createNotificationChannel(channel)
    }
}