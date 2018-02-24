/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.remote.SiaApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScValueRepository
@Inject constructor(
        private val api: SiaApi,
        private val db: AppDatabase
) {
    fun updateScValue() = api.getScPrice().doOnSuccess {
        db.scValueDao().insertReplaceOnConflict(it)
    }.toCompletable()!!

    fun mostRecent() = db.scValueDao().mostRecent()
}