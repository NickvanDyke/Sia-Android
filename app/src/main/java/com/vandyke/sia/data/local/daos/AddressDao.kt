/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import androidx.room.Dao
import androidx.room.Query
import com.vandyke.sia.data.models.wallet.AddressData
import io.reactivex.Single

@Dao
interface AddressDao : BaseDao<AddressData> {
    @Query("SELECT * FROM addresses ORDER BY Random() LIMIT 1")
    fun getAddress(): Single<AddressData>

    @Query("SELECT * FROM addresses")
    fun getAll(): Single<List<AddressData>>

    @Query("DELETE FROM addresses")
    fun deleteAll()
}