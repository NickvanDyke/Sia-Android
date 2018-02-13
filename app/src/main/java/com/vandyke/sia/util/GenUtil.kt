/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import java.math.BigDecimal
import java.math.RoundingMode


object GenUtil {

    fun readableFilesizeString(filesize: BigDecimal): String {
        var size = filesize
        var i = 0
        val kilo = BigDecimal("1024")
        while (size > kilo && i < 6) {
            size = size.divide(kilo, 10, RoundingMode.HALF_UP)
            i++
        }
        val sizeString = when (i) {
            0 -> "B"
            1 -> "KB"
            2 -> "MB"
            3 -> "GB"
            4 -> "TB"
            5 -> "PB"
            6 -> "EB"
            else -> "Super big"
        }

        return String.format("%.${Prefs.displayedDecimalPrecision}f %s", size, sizeString)
    }

    fun launchCustomTabs(context: Context, url: String) {
        CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .build()
                .launchUrl(context, Uri.parse(url))
    }

    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}
