/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.repository

import com.vandyke.siamobile.data.remote.siaApi
import com.vandyke.siamobile.db

class ScValueRepository {
    fun updateScValue() = siaApi.getScPrice().doAfterSuccess {
        db.scValueDao().insert(it)
    }.toCompletable()

    fun scValue() = db.scValueDao().mostRecent()
}