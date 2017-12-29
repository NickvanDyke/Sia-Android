/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.local

import com.chibatching.kotpref.KotprefModel

object Prefs : KotprefModel() {
    override val kotprefName: String = "${context.packageName}_preferences"

    var darkMode by booleanPref(false)
    var startupPage by stringPref("wallet")
    var hideZero by booleanPref(true)
    var useExternal by booleanPref(false)
    var displayedDecimalPrecision by intPref(2)
    var SiaNodeWakeLock by booleanPref(false)
    var startSiaAutomatically by booleanPref(true)
    var firstRunEver by booleanPref(true)
}