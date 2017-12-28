/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.intellij.lang.annotations.Language
import vandyke.siamobile.data.models.wallet.WalletData

@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wallet: WalletData)

    @Language("RoomSql")
    @Query("SELECT a.* FROM wallet a LEFT OUTER JOIN wallet b ON a.timestamp < b.timestamp WHERE b.timestamp IS NULL")
    fun getMostRecent(): Flowable<WalletData>

    @Query("DELETE FROM wallet")
    fun deleteAll()
}