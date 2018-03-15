/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.vandyke.sia.data.models.wallet.TransactionData
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface TransactionDao : BaseDao<TransactionData> {
    @Query("SELECT * FROM transactions")
    fun getAll(): Single<List<TransactionData>>

    @Query("SELECT * FROM transactions ORDER BY confirmationtimestamp DESC")
    fun allByMostRecent(): Flowable<List<TransactionData>>

    @Query("DELETE FROM transactions")
    fun deleteAll()
}