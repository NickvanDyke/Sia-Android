/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.repository

import com.vandyke.siamobile.data.remote.siaApi
import com.vandyke.siamobile.db

class ConsensusRepository {
    fun updateConsensus() = siaApi.consensus().doAfterSuccess {
        db.consensusDao().insert(it)
    }.toCompletable()

    fun consensus() = db.consensusDao().mostRecent()
}