/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import com.chibatching.kotpref.Kotpref
import vandyke.siamobile.ui.settings.GlobalPrefsListener
import vandyke.siamobile.ui.settings.Prefs
import vandyke.siamobile.util.NotificationUtil

class App : Application() {

    lateinit var globalPrefsListener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate() {
        NotificationUtil.createSiaNotificationChannel(this)
        val abi = Build.SUPPORTED_ABIS[0]
        if (abi != "arm64-v8a")
            throw TODO("Running on non-arm64-v8a")

        /* preferences stuff */
        Kotpref.init(this)
        globalPrefsListener = GlobalPrefsListener(this)
        Prefs.preferences.registerOnSharedPreferenceChangeListener(globalPrefsListener)
        super.onCreate()
    }
}
