/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile

import android.app.Application
import android.os.Build
import com.chibatching.kotpref.Kotpref
import vandyke.siamobile.util.NotificationUtil

class SiaMobileApplication : Application() {
    companion object {
        lateinit var abi: String
        lateinit var abi32: String
    }

    override fun onCreate() {
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
        Kotpref.init(this)
        super.onCreate()
    }
}
