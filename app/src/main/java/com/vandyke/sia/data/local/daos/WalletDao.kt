/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.vandyke.sia.data.models.wallet.WalletData
import io.reactivex.Flowable
import org.intellij.lang.annotations.Language

@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wallet: WalletData)

    @Language("RoomSql")
    @Query("SELECT a.* FROM wallet a LEFT OUTER JOIN wallet b ON a.timestamp < b.timestamp WHERE b.timestamp IS NULL")
    fun mostRecent(): Flowable<WalletData>

    @Query("DELETE FROM wallet")
    fun deleteAll()
}