/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import com.vandyke.sia.data.SiaError
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Subscribes on Schedulers.io(), observes on AndroidSchedulers.mainThread(), and when
 * the observable emits an error, it constructs a SiaError from it and calls the given
 * onError function with that
 */
fun Completable.siaSubscribe(
        onComplete: (() -> Unit)? = null,
        onError: ((SiaError) -> Unit)? = null
): Disposable {
    return this.io().main().sub(onComplete, onError)
}

fun <T> Single<T>.siaSubscribe(
        onSuccess: ((T) -> Unit)? = null,
        onError: ((SiaError) -> Unit)? = null
): Disposable {
    return this.io().main().sub(onSuccess, onError)
}

fun <T> Flowable<T>.siaSubscribe(
        onNext: ((T) -> Unit)? = null,
        onError: ((SiaError) -> Unit)? = null,
        onComplete: (() -> Unit)? = null
): Disposable {
    return this.io().main().sub(onNext, onError, onComplete)
}

fun Completable.io() = this.subscribeOn(Schedulers.io())!!

fun <T> Single<T>.io() = this.subscribeOn(Schedulers.io())!!

fun <T> Flowable<T>.io() = this.subscribeOn(Schedulers.io())!!


fun Completable.main() = this.observeOn(AndroidSchedulers.mainThread())!!

fun <T> Single<T>.main() = this.observeOn(AndroidSchedulers.mainThread())!!

fun <T> Flowable<T>.main() = this.observeOn(AndroidSchedulers.mainThread())!!


fun Completable.siaError() = this.onErrorResumeNext { Completable.error(com.vandyke.sia.data.SiaError(it)) }!!

fun <T> Single<T>.siaError() = this.onErrorResumeNext { Single.error(com.vandyke.sia.data.SiaError(it)) }!!

fun <T> Flowable<T>.siaError() = this.onErrorResumeNext { error: Throwable -> Flowable.error(com.vandyke.sia.data.SiaError(error)) }!!

/** when the observable emits an error, it constructs a SiaError from it and calls the given
  * onError function with that */
fun Completable.sub(onComplete: (() -> Unit)?, onError: ((SiaError) -> Unit)?) = when {
    onComplete != null && onError != null -> this.subscribe(onComplete, {
        if (it is SiaError)
            onError(it)
        else
            onError(SiaError(it))
    })
    // could pass an empty lambda for onError here if we wanted to avoid OnErrorNotImplementedException. That'd be confusing though I think
    onComplete != null -> this.subscribe(onComplete)
    onError != null -> this.subscribe({}, {
        if (it is SiaError)
            onError(it)
        else
            onError(SiaError(it))
    })
    else -> this.subscribe()
}!!

fun <T> Single<T>.sub(onSuccess: ((T) -> Unit)?, onError: ((SiaError) -> Unit)?) = when {
    onSuccess != null && onError != null -> this.subscribe(onSuccess, {
        if (it is SiaError)
            onError(it)
        else
            onError(SiaError(it))
    })
    onSuccess != null -> this.subscribe(onSuccess)
    onError != null -> this.subscribe({}, {
        if (it is SiaError)
            onError(it)
        else
            onError(SiaError(it))
    })
    else -> this.subscribe()
}!!

fun <T> Flowable<T>.sub(onSuccess: ((T) -> Unit)?, onError: ((SiaError) -> Unit)?, onComplete: (() -> Unit)?) = when {
    onSuccess != null && onError != null && onComplete != null -> this.subscribe(onSuccess, {
        if (it is SiaError)
            onError(it)
        else
            onError(SiaError(it))
    }, onComplete)
    onSuccess != null -> this.subscribe(onSuccess)
    onError != null -> this.subscribe({}, {
        if (it is SiaError)
            onError(it)
        else
            onError(SiaError(it))
    })
    else -> this.subscribe()
}!!