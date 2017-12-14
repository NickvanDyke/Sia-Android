/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.data.local

import com.chibatching.kotpref.KotprefModel

object Prefs : KotprefModel() {
    override val kotprefName: String = "${context.packageName}_preferences"

    var darkMode by booleanPref(false)
    var startupPage by stringPref("wallet")
    var hideZero by booleanPref(true)
    var useExternal by booleanPref(false)
    var displayedDecimalPrecision by intPref(2)
    var SiaNodeWakeLock by booleanPref(false)
    val renterDirs by stringSetPref()
    var coldStorageExists by booleanPref(false)
    var coldStorageSeed by stringPref()
    var coldStoragePassword by stringPref()
    val coldStorageAddresses by stringSetPref()
}