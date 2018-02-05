/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumOrdinalPref
import com.vandyke.sia.data.repository.FilesRepository

object Prefs : KotprefModel() {
    override val kotprefName: String = "${context.packageName}_preferences"

    /* make sure to update this when the included siad is updated */
    val siaVersion by stringPref("1.3.1")

    var timesStarted by intPref()
    var shownFeedbackDialog by booleanPref(false)

    var cachedPurchased by booleanPref(false)
    var viewedOnboarding by booleanPref(false)

    var darkMode by booleanPref(false)
    var apiPassword by stringPref()
    var startupPage by stringPref("wallet")
    var hideZero by booleanPref(true)
    var useExternal by booleanPref(false)
    var displayedDecimalPrecision by intPref(2)

    var runSiaInBackground by booleanPref(true)
    var runSiaOnData by booleanPref(false)

    var ascending by booleanPref(true)
    var orderBy by enumOrdinalPref(FilesRepository.OrderBy.PATH)
}