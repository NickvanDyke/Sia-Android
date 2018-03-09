/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.vandyke.sia.data.models.renter.PricesData
import io.reactivex.Flowable

@Dao
interface PricesDao : BaseDao<PricesData> {
    @Query("SELECT a.* FROM prices a LEFT OUTER JOIN prices b ON a.timestamp < b.timestamp WHERE b.timestamp IS NULL")
    fun mostRecent(): Flowable<PricesData>

    @Query("DELETE FROM scValue")
    fun deleteAll()
}