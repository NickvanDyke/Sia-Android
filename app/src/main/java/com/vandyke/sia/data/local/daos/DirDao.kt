/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.vandyke.sia.data.local.data.renter.Dir
import io.reactivex.Flowable
import org.intellij.lang.annotations.Language

@Dao
interface DirDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnoreConflict(dir: Dir)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAbortIfConflict(dir: Dir)

    @Language("RoomSql")
    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%'")
    fun getImmediateDirs(path: String): Flowable<List<Dir>>

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