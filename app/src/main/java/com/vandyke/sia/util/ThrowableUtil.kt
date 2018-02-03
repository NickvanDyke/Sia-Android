/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.util.Log
import android.view.View
import com.vandyke.sia.data.remote.SiaException
import io.reactivex.exceptions.CompositeException

/* so that we can customize error messages for non-SiaExceptions.
 * Could probably require a context here and then be able to retrieve
 * localized/translated strings too. If this is a SiaException, then should
 * call a method on it that takes a context and uses it to call getString with
 * a string resource identifier that it's passed in its constructor */
fun Throwable.customMsg(): String {
    return when (this) {
        /* careful that this doesn't accidentally swallow important error messages */
        is CompositeException -> this.exceptions[0].localizedMessage
        else -> {
            if (this !is SiaException)
                Log.d("LOOK", "customMsg() called on ${this.javaClass.simpleName} without a custom msg implemented")
            localizedMessage
        }
    }
}

fun Throwable.snackbar(view: View) {
    SnackbarUtil.showSnackbar(view, this.customMsg())
}