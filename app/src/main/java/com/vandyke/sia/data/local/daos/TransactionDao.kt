/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.vandyke.sia.data.models.wallet.TransactionData
import io.reactivex.Single

@Dao
interface TransactionDao : BaseDao<TransactionData> {
    @Query("SELECT * FROM transactions ORDER BY transactionid")
    fun getAllById(): Single<List<TransactionData>>

    @Query("SELECT * FROM transactions WHERE netValue != 0 ORDER BY confirmationtimestamp DESC")
    fun allByMostRecent(): DataSource.Factory<Int, TransactionData>

    @Query("DELETE FROM transactions")
    fun deleteAll()
}