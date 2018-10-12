package com.vandyke.sia.data.local.daos

import androidx.room.Dao
import androidx.room.Query
import com.vandyke.sia.data.models.wallet.SeedData
import io.reactivex.Single

@Dao
interface SeedDao : BaseDao<SeedData> {

    @Query("SELECT * FROM seeds")
    fun getAll(): Single<List<SeedData>>

    @Query("DELETE FROM seeds")
    fun deleteAll()
}

