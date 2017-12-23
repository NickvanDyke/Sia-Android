/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile

import android.app.Application
import android.arch.persistence.room.Room
import android.os.Build
import com.chibatching.kotpref.Kotpref
import vandyke.siamobile.data.local.AppDatabase
import vandyke.siamobile.util.NotificationUtil

lateinit var db: AppDatabase

class App : Application() {

    override fun onCreate() {
        NotificationUtil.createSiaNotificationChannel(this)
        val abi = Build.SUPPORTED_ABIS[0]
//        if (abi != "arm64-v8a")
//            throw TODO("Running on non-arm64-v8a")

        /* preferences stuff */
        Kotpref.init(this)
        db = Room.databaseBuilder(this, AppDatabase::class.java, "db").build() // TODO: remove main thread queries
        super.onCreate()
    }
}
