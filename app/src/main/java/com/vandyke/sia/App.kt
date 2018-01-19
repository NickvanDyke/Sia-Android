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
import io.reactivex.subjects.PublishSubject

lateinit var appComponent: AppComponent
// TODO: inject these where they're used
val siadOutput = PublishSubject.create<String>()!!


class App : Application() {

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
