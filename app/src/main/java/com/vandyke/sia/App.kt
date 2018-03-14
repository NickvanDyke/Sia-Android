/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia

import android.app.Application
import android.content.Context
import com.chibatching.kotpref.Kotpref
import com.vandyke.sia.dagger.AppComponent
import com.vandyke.sia.dagger.AppModule
import com.vandyke.sia.dagger.DaggerAppComponent
import com.vandyke.sia.util.Analytics
import com.vandyke.sia.util.NotificationUtil

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        NotificationUtil.createSiaNodeNotificationChannel(this)

        /* init singletons. TODO: make them injected instead */
        Kotpref.init(this)
        Analytics.init(this)

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

        super.onCreate()
    }
}

/* extension function for more conveniently retrieving the appComponent for injecting */
fun Context.getAppComponent(): AppComponent = (this.applicationContext as App).appComponent