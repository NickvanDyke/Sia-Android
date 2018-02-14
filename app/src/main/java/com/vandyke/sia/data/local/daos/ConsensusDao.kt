/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.vandyke.sia.data.models.consensus.ConsensusData
import io.reactivex.Flowable
import org.intellij.lang.annotations.Language

@Dao
interface ConsensusDao : BaseDao<ConsensusData> {
    @Language("RoomSql")
    @Query("SELECT a.* FROM consensus a LEFT OUTER JOIN consensus b ON a.timestamp < b.timestamp WHERE b.timestamp IS NULL")
    fun mostRecent(): Flowable<ConsensusData>
}