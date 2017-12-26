/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import org.intellij.lang.annotations.Language
import vandyke.siamobile.data.local.data.wallet.Address

@Dao
interface AddressDao {
    @Insert
    fun insert(address: Address)

    @Insert
    fun insertAll(addresses: List<Address>)

    @Language("RoomSql")
    @Query("SELECT * FROM addresses LIMIT 1")
    fun getAddress(): Single<Address>

    @Language("RoomSql")
    @Query("SELECT * FROM addresses")
    fun getAll(): Flowable<List<Address>>
}