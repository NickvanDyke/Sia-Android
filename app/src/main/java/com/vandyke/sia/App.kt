/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia

import android.app.Application
import android.arch.persistence.room.Room
import android.os.Build
import com.chibatching.kotpref.Kotpref
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.util.NotificationUtil
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

// TODO: proper stuff instead of this. They should probably be injected
lateinit var db: AppDatabase
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

        /* init the Pref singleton */
        Kotpref.init(this)

        // db stuff. TODO: some of this, like deleting all, is for testing. remove it eventually
        db = Room.databaseBuilder(this, AppDatabase::class.java, "db").fallbackToDestructiveMigration().build()
        launch(CommonPool) {
            db.fileDao().deleteAll()
            db.dirDao().deleteAll()
        }
        super.onCreate()

        println(Build.SUPPORTED_64_BIT_ABIS)
    }
}
