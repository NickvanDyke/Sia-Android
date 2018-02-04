/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.widget.Toast
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

    val isSiadSupported = Build.SUPPORTED_64_BIT_ABIS.any { it == "arm64-v8a" }

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
    
    fun showRateDialog(context: Context) {
        AlertDialog.Builder(context)
                .setTitle("Liking Sia for Android?")
                .setPositiveButton("Yes") { _, _ ->
                    AlertDialog.Builder(context)
                            .setMessage("Would you like to leave a rating? I'd really appreciate it.")
                            .setPositiveButton("Sure!") { _, _ ->
                                Toast.makeText(context, "Thank you!", Toast.LENGTH_LONG).show()
                                context.startActivity(Intents.playStore)
                            }.setNegativeButton("No thanks", null)
                            .show()
                }.setNegativeButton("No") { _, _ ->
                    AlertDialog.Builder(context)
                            .setMessage("Would you like to give feedback on why?")
                            .setPositiveButton("Yes") { _, _ ->
                                context.startActivity(Intents.emailMe
                                        .putExtra(Intent.EXTRA_SUBJECT, "Feedback"))
                            }.setNegativeButton("No thanks", null)
                            .show()
                }.show()
    }
}

fun <T> LiveData<T>.observe(owner: LifecycleOwner, onChanged: (T) -> Unit) {
    this.observe(owner, Observer {
        if (it != null)
            onChanged(it)
    })
}