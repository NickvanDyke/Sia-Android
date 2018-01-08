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
    fun dir(path: String): Flowable<Dir>

    @Query("SELECT * FROM dirs WHERE path = :path")
    fun getDir(path: String): Single<Dir>

    /* Room doesn't allow using variables for certain things, so we need different queries for each sorting method */
    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY name ASC, size ASC")
    fun dirsInDirByName(path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY name DESC, size DESC")
    fun dirsInDirByNameDesc(path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY size ASC, name ASC")
    fun dirsInDirBySize(path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY size DESC, name DESC")
    fun dirsInDirBySizeDesc(path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY modified ASC, name ASC")
    fun dirsInDirByModified(path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY modified DESC, name DESC")
    fun dirsInDirByModifiedDesc(path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY name ASC, size ASC")
    fun dirsWithNameUnderDirByName(name: String, path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY name DESC, size DESC")
    fun dirsWithNameUnderDirByNameDesc(name: String, path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY size ASC, name ASC")
    fun dirsWithNameUnderDirBySize(name: String, path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY size DESC, name DESC")
    fun dirsWithNameUnderDirBySizeDesc(name: String, path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY modified ASC, name ASC")
    fun dirsWithNameUnderDirByModified(name: String, path: String): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY modified DESC, name DESC")
    fun dirsWithNameUnderDirByModifiedDesc(name: String, path: String): Flowable<List<Dir>>

    @Query("DELETE FROM dirs")
    fun deleteAll()

    @Query("DELETE FROM dirs WHERE path = :path")
    fun deleteDir(path: String)

    @Query("DELETE FROM dirs WHERE path LIKE :path || '/%'")
    fun deleteDirsUnder(path: String)
}