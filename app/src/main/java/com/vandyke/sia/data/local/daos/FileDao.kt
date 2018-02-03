/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.vandyke.sia.data.models.renter.RenterFileData
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(file: RenterFileData)

    /* Room doesn't allow using variables for certain things, so we need different queries for each sorting method */
    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY name ASC, filesize ASC")
    fun filesInDirByName(path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY name DESC, filesize DESC")
    fun filesInDirByNameDesc(path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY filesize ASC, name ASC")
    fun filesInDirBySize(path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY filesize DESC, name DESC")
    fun filesInDirBySizeDesc(path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY modified ASC, name ASC")
    fun filesInDirByModified(path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND path NOT LIKE :path || '/%/%' ORDER BY modified DESC, name DESC")
    fun filesInDirByModifiedDesc(path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY name, filesize ASC")
    fun filesWithNameUnderDirByName(name: String, path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY name DESC, filesize DESC")
    fun filesWithNameUnderDirByNameDesc(name: String, path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY filesize ASC, name ASC")
    fun filesWithNameUnderDirBySize(name: String, path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY filesize DESC, name DESC")
    fun filesWithNameUnderDirBySizeDesc(name: String, path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY modified ASC, name ASC")
    fun filesWithNameUnderDirByModified(name: String, path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%' AND name LIKE '%' || :name || '%' ORDER BY modified DESC, name DESC")
    fun filesWithNameUnderDirByModifiedDesc(name: String, path: String): Flowable<List<RenterFileData>>

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%'")
    fun getFilesUnder(path: String): Single<List<RenterFileData>>

    @Query("DELETE FROM files")
    fun deleteAll()

    @Query("DELETE FROM files WHERE path == :path")
    fun deleteFile(path: String)

    @Query("DELETE FROM files WHERE path LIKE :path || '/%'")
    fun deleteFilesUnder(path: String)
}