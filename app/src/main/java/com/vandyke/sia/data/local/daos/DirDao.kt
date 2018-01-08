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
import io.reactivex.Single

@Dao
interface DirDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplaceIfConflict(dir: Dir)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAbortIfConflict(dir: Dir)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnoreIfConflict(dir: Dir)

    @Query("SELECT * FROM dirs")
    fun all(): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs")
    fun getAll(): Single<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path = :path")
    fun getDir(path: String): Single<Dir>

    /* ordered by name because otherwise the order of the list emitted by the flowable can swap for whatever reason,
     * causing unnecessary rearrangements in the UI */
    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY name")
    fun dirsInDirByName(path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY name DESC")
    fun dirsInDirByNameDesc(path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY size")
    fun dirsInDirBySize(path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY size DESC")
    fun dirsInDirBySizeDesc(path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY name")
    fun dirsWithNameUnderDir(name: String, path: String): Flowable<List<Dir>>

    @Query("DELETE FROM dirs")
    fun deleteAll()

    @Query("DELETE FROM dirs WHERE path = :path")
    fun deleteDir(path: String)

    @Query("DELETE FROM dirs WHERE path LIKE :path || '/%'")
    fun deleteDirsUnder(path: String)
}