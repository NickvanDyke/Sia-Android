/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util.rx

import androidx.room.RoomDatabase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers


fun Completable.inDbTransaction(db: RoomDatabase): Completable =
        this.doOnSubscribe { db.beginTransaction() }
                .doOnComplete { db.setTransactionSuccessful() }
                .doFinally { db.endTransaction() }

fun <T> Single<T>.inDbTransaction(db: RoomDatabase): Single<T> =
        this.doOnSubscribe { db.beginTransaction() }
                .doOnSuccess { db.setTransactionSuccessful() }
                .doFinally { db.endTransaction() }

fun <T : Any, R : Iterable<T>> Single<R>.toElementsObservable(): Observable<T> = this.flatMapObservable<T> { it.toObservable() }


fun Completable.track(tracker: MutableNonNullLiveData<Int>): Completable =
        this.doOnSubscribe { tracker.increment() }
                .doFinally { tracker.decrementZeroMin() }

fun <T> Single<T>.track(tracker: MutableNonNullLiveData<Int>): Single<T> =
        this.doOnSubscribe { tracker.increment() }
                .doFinally { tracker.decrementZeroMin() }

@JvmName("trackBool")
fun Completable.track(tracker: MutableNonNullLiveData<Boolean>): Completable =
        this.doOnSubscribe { tracker.value = true }
                .doFinally { tracker.value = false }

@JvmName("trackBool")
fun <T> Single<T>.track(tracker: MutableNonNullLiveData<Boolean>): Single<T> =
        this.doOnSubscribe { tracker.value = true }
                .doFinally { tracker.value = false }


fun Completable.io() = this.subscribeOn(Schedulers.io())!!

fun <T> Single<T>.io() = this.subscribeOn(Schedulers.io())!!

fun <T> Flowable<T>.io() = this.subscribeOn(Schedulers.io())!!

fun <T> Observable<T>.io() = this.subscribeOn(Schedulers.io())!!


fun Completable.main() = this.observeOn(AndroidSchedulers.mainThread())!!

fun <T> Single<T>.main() = this.observeOn(AndroidSchedulers.mainThread())!!

fun <T> Flowable<T>.main() = this.observeOn(AndroidSchedulers.mainThread())!!

fun <T> Observable<T>.main() = this.observeOn(AndroidSchedulers.mainThread())!!
