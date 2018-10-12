/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import androidx.room.Dao
import androidx.room.Query
import com.vandyke.sia.data.models.consensus.ConsensusData
import io.reactivex.Flowable

@Dao
interface ConsensusDao : BaseDao<ConsensusData> {
    @Query("SELECT a.* FROM consensus a LEFT OUTER JOIN consensus b ON a.timestamp < b.timestamp WHERE b.timestamp IS NULL")
    fun mostRecent(): Flowable<ConsensusData>

    @Query("DELETE FROM consensus")
    fun deleteAll()
}