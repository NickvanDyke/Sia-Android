/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationUtil {
    fun cancelNotification(context: Context, id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }
}

fun NotificationCompat.Builder.show(context: Context, id: Int) {
    NotificationManagerCompat.from(context).notify(id, this.build())
}

fun Notification.show(context: Context, id: Int) {
    NotificationManagerCompat.from(context).notify(id, this)
}