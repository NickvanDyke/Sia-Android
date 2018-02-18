/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.vandyke.sia.data.models.renter.RenterFinancialMetricsData
import io.reactivex.Flowable

@Dao
interface SpendingDao : BaseDao<RenterFinancialMetricsData> {
    @Query("SELECT a.* FROM spending a LEFT OUTER JOIN spending b ON a.timestamp < b.timestamp WHERE b.timestamp IS NULL")
    fun mostRecent(): Flowable<RenterFinancialMetricsData>
}