/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.util

import com.vandyke.siamobile.data.remote.SiaError
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
fun <T> Single<T>.siaSubscribe(onNext: (T) -> Unit, onError: (SiaError) -> Unit): Disposable {
    return this.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, {
                if (it is SiaError)
                    onError(it)
                else
                    onError(SiaError(it))
            })
}

fun Completable.siaSubscribe(onNext: () -> Unit, onError: (SiaError) -> Unit): Disposable {
    return this.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, {
                if (it is SiaError)
                    onError(it)
                else
                    onError(SiaError(it))
            })
}

fun <T> Flowable<T>.siaSubscribe(onNext: (T) -> Unit, onError: (SiaError) -> Unit): Disposable {
    return this.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, {
                if (it is SiaError)
                    onError(it)
                else
                    onError(SiaError(it))
            })
}