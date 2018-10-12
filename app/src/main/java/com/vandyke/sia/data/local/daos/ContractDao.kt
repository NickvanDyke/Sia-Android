/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import androidx.room.Dao
import androidx.room.Query
import com.vandyke.sia.data.models.renter.ContractData
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface ContractDao : BaseDao<ContractData> {
    @Query("SELECT * FROM contracts")
    fun all(): Flowable<List<ContractData>>

    @Query("SELECT * FROM contracts ORDER BY id")
    fun getAllById(): Single<List<ContractData>>
}