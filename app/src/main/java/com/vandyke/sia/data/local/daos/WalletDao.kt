/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.vandyke.sia.data.models.wallet.WalletData
import io.reactivex.Flowable

@Dao
interface WalletDao : BaseDao<WalletData> {

    @Query("SELECT a.* FROM wallet a LEFT OUTER JOIN wallet b ON a.timestamp < b.timestamp WHERE b.timestamp IS NULL")
    fun mostRecent(): Flowable<WalletData>

    @Query("SELECT * FROM wallet WHERE timestamp >= strftime('%s', datetime('now', '-30 days'))*1000 ORDER BY timestamp")
    fun allLastMonth(): Flowable<List<WalletData>>

    @Query("DELETE FROM wallet")
    fun deleteAll()
}