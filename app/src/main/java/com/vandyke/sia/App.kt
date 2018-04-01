/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia

import android.app.Application
import android.content.Context
import com.chibatching.kotpref.Kotpref
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.vandyke.sia.dagger.AppComponent
import com.vandyke.sia.dagger.AppModule
import com.vandyke.sia.dagger.DaggerAppComponent
import com.vandyke.sia.util.Analytics
import io.fabric.sdk.android.Fabric


class App : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        /* disable crash reporting for debug builds */
        Fabric.with(this, Crashlytics.Builder()
                .core(CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build())

        /* init singletons. TODO: inject them instead */
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