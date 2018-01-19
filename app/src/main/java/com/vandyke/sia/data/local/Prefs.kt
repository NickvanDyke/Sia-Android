/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumOrdinalPref
import com.vandyke.sia.data.repository.FilesRepository

object Prefs : KotprefModel() {
    override val kotprefName: String = "${context.packageName}_preferences"

    var darkMode by booleanPref(false)
    var apiPassword by stringPref()
    var startupPage by stringPref("wallet")
    var hideZero by booleanPref(true)
    var useExternal by booleanPref(false)
    var displayedDecimalPrecision by intPref(2)
    var SiaNodeWakeLock by booleanPref(false)
    var runSiaOnData by booleanPref()

    var ascending by booleanPref(true)
    var sortBy by enumOrdinalPref(FilesRepository.SortBy.NAME)
}