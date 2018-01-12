/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.vandyke.sia.data.models.wallet.ScValueData
import io.reactivex.Flowable

@Dao
interface ScValueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(scValueData: ScValueData)

    @Query("SELECT a.* FROM scValue a LEFT OUTER JOIN scValue b ON a.timestamp < b.timestamp WHERE b.timestamp IS NULL")
    fun mostRecent(): Flowable<ScValueData>
}