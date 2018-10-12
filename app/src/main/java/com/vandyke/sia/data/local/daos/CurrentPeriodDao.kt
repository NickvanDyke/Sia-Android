/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import androidx.room.Dao
import androidx.room.Query
import com.vandyke.sia.data.models.renter.CurrentPeriodData
import io.reactivex.Flowable

@Dao
interface CurrentPeriodDao : BaseDao<CurrentPeriodData> {
    @Query("SELECT * FROM currentPeriod LIMIT 1")
    fun onlyEntry(): Flowable<CurrentPeriodData>

    @Query("DELETE FROM currentPeriod")
    fun deleteAll()
}