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
import android.support.v4.widget.SwipeRefreshLayout
import android.util.TypedValue
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
                .setToolbarColor(context.getAttrColor(R.attr.colorPrimary))
                .build()
                .launchUrl(context, Uri.parse(url))
    }
}

fun Context.bitmapFromVector(drawableId: Int): Bitmap {
    val drawable = getDrawable(drawableId)
    val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

fun Context.getColorRes(resId: Int) = ContextCompat.getColor(this, resId)

fun Context.getAttrColor(attrResId: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(attrResId, typedValue, true)
    return typedValue.data
}

fun Context.getAttrColors(vararg attrResIds: Int): List<Int> {
    val array = this.theme.obtainStyledAttributes(attrResIds)
    return List(attrResIds.size, { index -> array.getColor(index, 0xFF0FF) })
}

fun SwipeRefreshLayout.setColors(context: Context) {
    val colors = context.getAttrColors(R.attr.colorAccent, android.R.attr.windowBackground)
    this.setColorSchemeColors(colors[0])
    this.setProgressBackgroundColorSchemeColor(colors[1])
}

inline fun <T> Iterable<T>.sumByBigDecimal(selector: (T) -> BigDecimal): BigDecimal {
    var sum = BigDecimal.ZERO
    for (element in this) {
        sum += selector(element)
    }
    return sum
}