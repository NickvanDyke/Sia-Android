/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.chibatching.kotpref.Kotpref
import com.vandyke.sia.dagger.AppComponent
import com.vandyke.sia.dagger.AppModule
import com.vandyke.sia.dagger.DaggerAppComponent
import com.vandyke.sia.data.siad.SiadSource
import com.vandyke.sia.util.NotificationUtil
import javax.inject.Inject

lateinit var appComponent: AppComponent

class App : Application() {

    @Inject
    lateinit var siadSource: SiadSource

    override fun onCreate() {
        NotificationUtil.createSiaNodeNotificationChannel(this)

        /* init the Prefs singleton */
        Kotpref.init(this)

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

        appComponent.inject(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
                siadSource.appInForeground = false
            }

            override fun onActivityResumed(activity: Activity?) {
                siadSource.appInForeground = true
            }

            override fun onActivityStarted(activity: Activity?) {}
            override fun onActivityDestroyed(activity: Activity?) {}
            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
            override fun onActivityStopped(activity: Activity?) {}
            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}

        })

        super.onCreate()
    }
}
