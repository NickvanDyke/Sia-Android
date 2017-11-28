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

class SiaMobileApplication : Application() {
    companion object {
        lateinit var abi: String
        lateinit var abi32: String
    }

    lateinit var globalPrefsListener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate() {
        NotificationUtil.createSiaNotificationChannel(this)
        abi = Build.SUPPORTED_ABIS[0]
        if ("arm" in abi)
            abi32 = "arm32"
        else if ("x86" in abi)
            abi32 = "x86"
        else
            abi32 = "idk"
        if (abi == "arm64-v8a")
            abi = "arm64"

        /* preferences stuff */
        Kotpref.init(this)
        globalPrefsListener = GlobalPrefsListener(this)
        Prefs.preferences.registerOnSharedPreferenceChangeListener(globalPrefsListener)
        super.onCreate()
    }
}
