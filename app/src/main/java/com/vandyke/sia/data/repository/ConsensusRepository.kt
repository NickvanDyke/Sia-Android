/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.remote.siaApi
import com.vandyke.sia.db

class ConsensusRepository {
    fun updateConsensus() = siaApi.consensus().doOnSuccess {
        db.consensusDao().insert(it)
    }.toCompletable()

    fun consensus() = db.consensusDao().mostRecent()
}