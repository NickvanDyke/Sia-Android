/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util.rx

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A lifecycle-aware observable that sends only new updates after subscription, used for events like
 * navigation and Snackbar messages.
 *
 *
 * This avoids a common problem with events: on configuration change (like rotation) an update
 * can be emitted if the observer is active. This LiveData only calls the observable if there's an
 * explicit call to setValue() or call().
 *
 *
 * Note that only one observer is going to be notified of changes.
 * Use (Mutable)LiveEvent for the same event behavior but with multiple observers.
 */
open class SingleLiveEvent<T> : LiveData<T>() {

    protected val pending = AtomicBoolean(false)

    @MainThread
    fun observe(owner: LifecycleOwner, onChanged: (T) -> Unit) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }

        super.observe(owner, Observer { t ->
            if (pending.compareAndSet(true, false)) {
                onChanged(t!!)
            }
        })
    }


    companion object {
        private const val TAG = "SingleLiveEvent"
    }
}