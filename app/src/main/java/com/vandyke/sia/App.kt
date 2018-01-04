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

// TODO: proper stuff instead of this
lateinit var db: AppDatabase
val siadOutput = PublishSubject.create<String>()!!
val isSiadLoaded = BehaviorSubject.create<Boolean>()!!
val isSiadServiceStarted = BehaviorSubject.create<Boolean>()!!



class App : Application() {

    override fun onCreate() {
        NotificationUtil.createSiaNotificationChannel(this)
        val abi = Build.SUPPORTED_ABIS[0]
//        if (abi != "arm64-v8a")
//            throw TODO("Running on non-arm64-v8a")

        /* preferences stuff */
        Kotpref.init(this)
        db = Room.databaseBuilder(this, AppDatabase::class.java, "db").fallbackToDestructiveMigration().build() // TODO: remove main thread queries
        launch(CommonPool) {
            db.fileDao().deleteAll()
            db.dirDao().deleteAll()
        }
        super.onCreate()
    }
}
