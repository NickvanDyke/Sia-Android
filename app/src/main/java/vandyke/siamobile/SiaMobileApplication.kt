/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile

import android.app.Application
import android.os.Build
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import org.acra.ACRA
import vandyke.siamobile.misc.Utils
import vandyke.siamobile.settings.Prefs

val prefs: Prefs by lazy {
    SiaMobileApplication.prefs!!
}

@org.acra.annotation.ReportsCrashes(mailTo = "siamobiledev@gmail.com", mode = org.acra.ReportingInteractionMode.DIALOG, resDialogText = vandyke.siamobile.R.string.crash_dialog_text, resDialogIcon = vandyke.siamobile.R.drawable.sia_logo_transparent, resDialogTitle = vandyke.siamobile.R.string.crash_dialog_title, resDialogCommentPrompt = vandyke.siamobile.R.string.crash_dialog_comment_prompt, resDialogTheme = vandyke.siamobile.R.style.AppTheme_Light) // optional. default is your application name
class SiaMobileApplication : Application() {
    companion object {
        var prefs: Prefs? = null
        var requestQueue: RequestQueue? = null
        var abi: String? = null
        var abi32: String? = null
    }

    override fun onCreate() {
        val hi = "hi"
        hi.contains("hi")
        ACRA.init(this)
        prefs = Prefs(applicationContext)
        requestQueue = Volley.newRequestQueue(this)
        Utils.createSiaNotificationChannel(this)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            abi = Build.CPU_ABI
        else
            abi = Build.SUPPORTED_ABIS[0]
        if ("arm" in abi!!)
            abi32 = "arm32"
        else if ("x86" in abi!!)
            abi32 = "x86"
        if (abi == "arm64-v8a")
            abi = "arm64"
        // TODO: maybe add mips binaries
        super.onCreate()
    }
}
