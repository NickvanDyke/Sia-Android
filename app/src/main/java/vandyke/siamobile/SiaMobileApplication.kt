/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile

import android.app.Application
import android.os.Build
import org.acra.ACRA
import vandyke.siamobile.settings.Prefs
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


@org.acra.annotation.ReportsCrashes(mailTo = "siamobiledev@gmail.com", mode = org.acra.ReportingInteractionMode.DIALOG, resDialogText = vandyke.siamobile.R.string.crash_dialog_text, resDialogIcon = vandyke.siamobile.R.drawable.sia_logo_transparent, resDialogTitle = vandyke.siamobile.R.string.crash_dialog_title, resDialogCommentPrompt = vandyke.siamobile.R.string.crash_dialog_comment_prompt, resDialogTheme = vandyke.siamobile.R.style.AppTheme_Light) // optional. default is your application name
class SiaMobileApplication : Application() {
    companion object {
        lateinit var prefs: Prefs
        lateinit var abi: String
        lateinit var abi32: String
    }

    override fun onCreate() {
        ACRA.init(this)
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
        if (abi == "arm64-v8a")
            abi = "arm64"
        // TODO: maybe add mips binaries
        super.onCreate()
    }
}
