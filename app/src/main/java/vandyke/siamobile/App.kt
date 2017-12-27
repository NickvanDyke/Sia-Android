/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile

import android.app.Application
import android.arch.persistence.room.Room
import android.os.Build
import com.chibatching.kotpref.Kotpref
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import vandyke.siamobile.data.local.AppDatabase
import vandyke.siamobile.util.NotificationUtil

// TODO: proper stuff instead of this
lateinit var db: AppDatabase
val siadOutput = PublishSubject.create<String>()!!
val isSiadLoaded = BehaviorSubject.create<Boolean>()!!


class App : Application() {

    override fun onCreate() {
        NotificationUtil.createSiaNotificationChannel(this)
        val abi = Build.SUPPORTED_ABIS[0]
//        if (abi != "arm64-v8a")
//            throw TODO("Running on non-arm64-v8a")

        /* preferences stuff */
        Kotpref.init(this)
        db = Room.databaseBuilder(this, AppDatabase::class.java, "db").fallbackToDestructiveMigration().build() // TODO: remove main thread queries
        super.onCreate()
    }
}
