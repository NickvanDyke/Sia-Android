/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.vandyke.sia.data.models.wallet.TransactionData
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplaceOnConflict(tx: TransactionData)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnoreOnConflict(tx: TransactionData)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(txs: List<TransactionData>)

    @Query("SELECT * FROM transactions")
    fun getAll(): Single<List<TransactionData>>

    @Query("SELECT * FROM transactions ORDER BY confirmationTimestamp DESC")
    fun allByMostRecent(): Flowable<List<TransactionData>>

    @Query("DELETE FROM transactions")
    fun deleteAll()

    @Query("DELETE FROM transactions WHERE transactionId == :id")
    fun delete(id: String)
}