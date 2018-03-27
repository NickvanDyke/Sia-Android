/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util.rx

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer

fun <T> LiveData<T>.observe(owner: LifecycleOwner, onChanged: (T) -> Unit) {
    this.observe(owner, Observer {
        if (it != null)
            onChanged(it)
    })
}

fun <T> LiveData<T>.observeForevs(onChanged: (T) -> Unit) {
    this.observeForever {
        if (it == null)
            throw IllegalStateException()
        else
            onChanged(it)
    }
}

fun NonNullLiveData<Int>.increment() {
    this.value = this.value + 1
}

fun NonNullLiveData<Int>.decrementZeroMin() {
    if (this.value > 0)
        this.value = this.value - 1
}