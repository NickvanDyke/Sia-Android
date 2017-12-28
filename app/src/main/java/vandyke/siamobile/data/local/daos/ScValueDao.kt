/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import vandyke.siamobile.data.models.wallet.ScValueData

@Dao
interface ScValueDao {
    @Insert
    fun insert(scValueData: ScValueData)

    @Query("SELECT a.* FROM scValue a LEFT OUTER JOIN scValue b ON a.timestamp < b.timestamp WHERE b.timestamp IS NULL")
    fun mostRecent(): Flowable<ScValueData>
}