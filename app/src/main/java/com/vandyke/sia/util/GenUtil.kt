/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.util.TypedValue
import android.widget.ProgressBar
import com.vandyke.sia.R
import java.math.BigDecimal


object GenUtil {
    fun launchCustomTabs(context: Context, url: String) {
        CustomTabsIntent.Builder()
                .setToolbarColor(context.getAttrColor(R.attr.colorPrimary))
                .build()
                .launchUrl(context, Uri.parse(url))
    }
}

/** steps through each list, calling the supplied functions depending on their contents relative to each other.
  * Both lists MUST be sorted in the same order by the same criteria for this to work properly. */
/* This is used by both the Files and Wallet Repository to bring db txs/files into line with api txs/files.
 * I'm fairly confident that it's close to the most efficient way to do that. Over time, the number of transactions
 * (and possibly files) will continue to grow, so I wanted an efficient way to do this. I considered DiffUtil,
 * but based on its documentation, that would have been much more complex, and maybe not even faster (if at all). */
inline fun <T, R : Comparable<*>> List<T>.diffWith(
        other: List<T>,
        crossinline selector: (T) -> R?,
        crossinline onBothHave: (T, T) -> Unit,
        crossinline onThisHas: (T) -> Unit,
        crossinline onOtherHas: (T) -> Unit
) {
    val comparator = compareBy(selector)
    var index1 = 0
    var index2 = 0
    while (index1 < this.size && index2 < other.size) {
        val item1 = this[index1]
        val item2 = other[index2]
        when (comparator.compare(item1, item2)) {
        /* both lists have the items */
            0 -> {
                index1++
                index2++
                onBothHave(item1, item2)
            }

        /* other has an item that this doesn't have */
            1 -> {
                index2++
                onOtherHas(item2)
            }

        /* this has an item that other doesn't have */
            -1 -> {
                index1++
                onThisHas(item1)
            }
        }
    }

    for (i in index1 until this.size)
        onThisHas(this[i])
    for (i in index2 until other.size)
        onOtherHas(other[i])
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

fun Context.havePermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
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

fun String.replaceLast(oldValue: String, newValue: String): String {
    val index = this.lastIndexOf(oldValue)
    return if (index != -1)
        this.substring(0, index) + newValue + this.substring(index + oldValue.length)
    else
        this
}

fun ProgressBar.setIndeterminateColorRes(colorRes: Int) {
    this.indeterminateDrawable.setColorFilter(ContextCompat.getColor(context!!, colorRes), PorterDuff.Mode.SRC_IN)
}

fun ProgressBar.setProgressColorRes(colorRes: Int) {
    this.progressDrawable.setColorFilter(ContextCompat.getColor(context!!, colorRes), PorterDuff.Mode.SRC_IN)
}
