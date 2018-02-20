/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia

import android.app.Application
import com.chibatching.kotpref.Kotpref
import com.vandyke.sia.dagger.AppComponent
import com.vandyke.sia.dagger.AppModule
import com.vandyke.sia.dagger.DaggerAppComponent
import com.vandyke.sia.util.Analytics
import com.vandyke.sia.util.NotificationUtil

lateinit var appComponent: AppComponent

class App : Application() {

    override fun onCreate() {
        NotificationUtil.createSiaNodeNotificationChannel(this)

        /* init singletons */
        Kotpref.init(this)
        Analytics.init(this)

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

        super.onCreate()
    }
}
