/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.database.sqlite.SQLiteConstraintException
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import com.vandyke.sia.BuildConfig
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.remote.SiaException
import com.vandyke.sia.data.remote.SiadNotRunning
import com.vandyke.sia.data.siad.SiadStatus
import io.github.tonnyl.light.Light
import io.reactivex.exceptions.CompositeException
import java.net.UnknownHostException

/* so that we can customize error messages for non-SiaExceptions.
 * Could probably require a context here and then be able to retrieve
 * localized/translated strings too. If this is a SiaException, then should
 * call a method on it that takes a context and uses it to call getString with
 * a string resource identifier that it's passed in its constructor */
fun Throwable.customMsg(): String? {
    return when (this) {
        is CompositeException -> {
            if (exceptions.all { it.javaClass == exceptions[0].javaClass }) {
                exceptions[0].customMsg()
            } else {
                var msg = "Multiple errors -"
                exceptions.forEachIndexed { index, throwable ->
                    msg += " $index: ${throwable.customMsg()}"
                }
                msg
            }
        }
        is SQLiteConstraintException -> {
            if (BuildConfig.DEBUG)
                localizedMessage
            else
                "Database conflict. Try Settings > Clear cached data if this persists."
        }
        is UnknownHostException -> {
            val msg = this.message ?: return null
            val address = msg.substring(msg.indexOf('"') + 1, msg.lastIndexOf('"'))
            return "Couldn't resolve hostname \"$address\""
        }
        else -> {
            if (this !is SiaException)
                Log.d("CustomMsg", "customMsg() called on ${this.javaClass.simpleName} without a custom text implemented")
            localizedMessage
        }
    }
}

fun CompositeException.all(clazz: Class<*>) = this.exceptions.all { it.javaClass == clazz }

fun Throwable.snackbar(view: View, state: SiadStatus.State, length: Int = Snackbar.LENGTH_SHORT) {
    Light.error(view, this.customMsg() ?: "Error", length).apply {
        if ((this@snackbar is SiadNotRunning || (this@snackbar as? CompositeException)?.all(SiadNotRunning::class.java) == true)
                && state == SiadStatus.State.MANUALLY_STOPPED) {
            setAction("Start") { Prefs.siaManuallyStopped = false }
            setActionTextColor(view.context.getColorRes(android.R.color.white))
        }
    }.show()
}