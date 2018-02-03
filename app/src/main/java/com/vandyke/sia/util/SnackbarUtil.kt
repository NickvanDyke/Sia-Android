/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import com.vandyke.sia.R
// TODO: queue snackbars, or don't show duplicates, or something like that
object SnackbarUtil {
    fun showSnackbar(view: View?, text: String, duration: Int = Snackbar.LENGTH_SHORT) {
        if (view == null || !view.isShown)
            return
        buildSnackbar(view, text, duration).show()
    }

    fun buildSnackbar(view: View, text: String, duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
        val snackbar = Snackbar.make(view, text, duration)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.colorAccent))
        return snackbar
    }
}