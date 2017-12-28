/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import org.intellij.lang.annotations.Language
import vandyke.siamobile.data.models.wallet.AddressData

@Dao
interface AddressDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(address: AddressData)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(addresses: List<AddressData>)

    @Language("RoomSql")
    @Query("SELECT * FROM addresses ORDER BY Random() LIMIT 1")
    fun getAddress(): Single<AddressData>

    @Language("RoomSql")
    @Query("SELECT * FROM addresses")
    fun getAll(): Flowable<List<AddressData>>

    @Query("DELETE FROM addresses")
    fun deleteAll()
}