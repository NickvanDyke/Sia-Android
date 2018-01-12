/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia

import android.app.Application
import com.chibatching.kotpref.Kotpref
import com.vandyke.sia.dagger.AppComponent
import com.vandyke.sia.dagger.AppModule
import com.vandyke.sia.dagger.DaggerAppComponent
import com.vandyke.sia.util.NotificationUtil
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

lateinit var appComponent: AppComponent
// TODO: inject these where they're used
val siadOutput = PublishSubject.create<String>()!!
val isSiadLoaded = BehaviorSubject.create<Boolean>()!!
val isSiadServiceStarted = BehaviorSubject.create<Boolean>()!!
val isSiadProcessStarting = BehaviorSubject.create<Boolean>()!!


class App : Application() {

    init {
        isSiadLoaded.onNext(false)
        isSiadServiceStarted.onNext(false)
    }

    override fun onCreate() {
        NotificationUtil.createSiaNodeNotificationChannel(this)

        /* init the Prefs singleton */
        Kotpref.init(this)

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

        super.onCreate()
    }
}
