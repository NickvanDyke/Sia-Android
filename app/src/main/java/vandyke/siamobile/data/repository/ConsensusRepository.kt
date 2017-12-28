/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.repository

import vandyke.siamobile.data.remote.siaApi
import vandyke.siamobile.db

class ConsensusRepository {
    fun updateConsensus() = siaApi.consensus().doAfterSuccess {
        db.consensusDao().insert(it)
    }.toCompletable()

    fun getConsensus() = db.consensusDao().getMostRecent()
}