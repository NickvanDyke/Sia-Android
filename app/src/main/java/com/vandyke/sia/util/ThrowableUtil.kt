/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import com.vandyke.sia.data.remote.SiaException
import io.github.tonnyl.light.Light
import io.reactivex.exceptions.CompositeException

/* so that we can customize error messages for non-SiaExceptions.
 * Could probably require a context here and then be able to retrieve
 * localized/translated strings too. If this is a SiaException, then should
 * call a method on it that takes a context and uses it to call getString with
 * a string resource identifier that it's passed in its constructor */
fun Throwable.customMsg(): String {
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
        else -> {
            if (this !is SiaException)
                Log.d("CustomMsg", "customMsg() called on ${this.javaClass.simpleName} without a custom text implemented")
            localizedMessage
        }
    }
}

fun Throwable.snackbar(view: View, length: Int = Snackbar.LENGTH_SHORT) {
    Light.error(view, this.customMsg(), length).show()
}