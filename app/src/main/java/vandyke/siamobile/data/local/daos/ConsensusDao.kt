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
import vandyke.siamobile.data.models.consensus.ConsensusData

@Dao
interface ConsensusDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(consensusData: ConsensusData)

    @Language("RoomSql")
    @Query("SELECT a.* FROM consensus a LEFT OUTER JOIN consensus b ON a.timestamp < b.timestamp WHERE b.timestamp IS NULL")
    fun getMostRecent(): Flowable<ConsensusData>
}