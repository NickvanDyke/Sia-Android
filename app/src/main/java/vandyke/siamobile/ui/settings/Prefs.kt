/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.settings

import com.chibatching.kotpref.KotprefModel

object Prefs : KotprefModel() {
    override val kotprefName: String = "${context.packageName}_preferences"

    var darkMode by booleanPref(false)
    var operationMode by stringPref("local_full_node")
    var address by stringPref("localhost:9980")
    var remoteAddress by stringPref("192.168.1.100:9980")
    var runLocalNodeOffWifi by booleanPref(false)
    var localNodeMinBattery by intPref(20)
    var firstTime by booleanPref(true)
    var startupPage by stringPref("wallet")
    var transparentBars by booleanPref(false)
    var customBgBase64 by stringPref()
    var hideZero by booleanPref(false)
    var useExternal by booleanPref(false)
    var apiPass by stringPref()
    var displayedDecimalPrecision by intPref(2)
    var coldStorageExists by booleanPref(false)
    var coldStorageSeed by stringPref()
    var coldStoragePassword by stringPref()
    val coldStorageAddresses by stringSetPref()
}