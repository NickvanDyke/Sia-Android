/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.remote.siaApi
import com.vandyke.sia.db

class ScValueRepository {
    fun updateScValue() = siaApi.getScPrice().doOnSuccess {
        db.scValueDao().insert(it)
    }.toCompletable()

    fun scValue() = db.scValueDao().mostRecent()
}