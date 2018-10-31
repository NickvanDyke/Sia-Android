/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util.rx

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers

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

fun MutableNonNullLiveData<Int>.increment() {
    this.value = this.value + 1
}

fun MutableNonNullLiveData<Int>.decrementZeroMin() {
    if (this.value > 0)
        this.value = this.value - 1
}

fun <T> LiveData<T>.toFlowable(): Flowable<T> {
    return Flowable.fromPublisher<T> { subscriber ->
        this.observeForevs {
            subscriber.onNext(it)
        }
    }.subscribeOn(AndroidSchedulers.mainThread())
}