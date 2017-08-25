/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile

import android.app.Application
import android.os.Build
import vandyke.siamobile.ui.settings.Prefs
import vandyke.siamobile.util.NotificationUtil

//object prefs: KotprefModel() {
//    var theme by stringPref()
//    var operationMode by stringPref()
//    var address by stringPref()
//    var remoteAddress by stringPref()
//    var refreshInterval by intPref()
//    var runLocalNodeOffWifi by booleanPref()
//    var localNodeMinBattery by intPref()
//    var runInBackground by booleanPref()
//    var firstTime by booleanPref()
//    var startupPage by stringPref()
//    var transparentBars by booleanPref()
//    var customBgBase64 by stringPref()
//    var hideZero by booleanPref()
//    var useExternal by booleanPref()
//    var feesEnabled by booleanPref()
//    var apiPass by stringPref()
//    var mostRecentTxId by stringPref()
//    var displayedDecimalPrecision by intPref()
//    var coldStorageExists by booleanPref()
//    var coldStorageSeed by stringPref()
//    var coldStoragePassword by stringPref()
//}

val prefs: Prefs by lazy {
    SiaMobileApplication.prefs
}


class SiaMobileApplication : Application() {
    companion object {
        lateinit var prefs: Prefs
        lateinit var abi: String
        lateinit var abi32: String
    }

    override fun onCreate() {
        prefs = Prefs(applicationContext)
        NotificationUtil.createSiaNotificationChannel(this)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            abi = Build.CPU_ABI
        else
            abi = Build.SUPPORTED_ABIS[0]
        if ("arm" in abi)
            abi32 = "arm32"
        else if ("x86" in abi)
            abi32 = "x86"
        else
            abi32 = "idk"
        if (abi == "arm64-v8a")
            abi = "arm64"
        // TODO: maybe add mips binaries
        super.onCreate()
    }
}
