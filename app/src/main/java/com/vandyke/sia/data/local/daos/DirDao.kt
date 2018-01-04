/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.vandyke.sia.data.local.models.renter.Dir
import io.reactivex.Flowable
import org.intellij.lang.annotations.Language

@Dao
interface DirDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplaceIfConflict(dir: Dir)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAbortIfConflict(dir: Dir)

    /* ordered by path because otherwise the order of the list emitted by the flowable can swap during inserts,
     * causing unnecessary rearrangements in the UI */
    @Language("RoomSql")
    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY path")
    fun immediateChildren(path: String): Flowable<List<Dir>>

    @Language("RoomSql")
    @Query("DELETE FROM dirs")
    fun deleteAll()

    @Language("RoomSql")
    @Query("DELETE FROM dirs WHERE path == :path")
    fun deleteDir(path: String)

    @Language("RoomSql")
    @Query("DELETE FROM dirs WHERE path LIKE :path || '/%'")
    fun deleteDirsUnder(path: String)
}