/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import androidx.room.Dao
import androidx.room.Query
import com.vandyke.sia.data.models.renter.RenterSettingsAllowanceData
import io.reactivex.Flowable

@Dao
interface AllowanceDao : BaseDao<RenterSettingsAllowanceData> {
    @Query("SELECT * FROM allowance LIMIT 1")
    fun onlyEntry(): Flowable<RenterSettingsAllowanceData>

    @Query("DELETE FROM allowance")
    fun deleteAll()
}