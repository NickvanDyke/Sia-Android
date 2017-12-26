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
import vandyke.siamobile.data.local.data.wallet.Transaction

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(tx: Transaction)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(txs: List<Transaction>)

    @Language("RoomSql")
    @Query("SELECT * FROM transactions ORDER BY confirmationTimestamp DESC")
    fun getAllByMostRecent(): Flowable<List<Transaction>>

    @Language("RoomSql")
    @Query("DELETE FROM transactions")
    fun deleteAll()

    @Language("RoomSql")
    @android.arch.persistence.room.Transaction
    fun deleteAllAndInsert(txs: List<Transaction>) {
        deleteAll()
        insertAll(txs)
    }
}