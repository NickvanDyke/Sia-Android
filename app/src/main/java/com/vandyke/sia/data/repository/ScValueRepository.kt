/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.remote.SiaApiInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScValueRepository
@Inject constructor(
        private val api: SiaApiInterface,
        private val db: AppDatabase
) {
    fun updateScValue() = api.getScPrice().doOnSuccess {
        db.scValueDao().insert(it)
    }.toCompletable()!!

    fun scValue() = db.scValueDao().mostRecent()
}