/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.*
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.data.repository.FilesRepository
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
abstract class FileDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(file: RenterFileData)

    @RawQuery(observedEntities = [RenterFileData::class])
    abstract fun customQuery(query: String): Flowable<List<RenterFileData>>

    fun getFiles(path: String, name: String? = null, orderBy: FilesRepository.OrderBy? = null, ascending: Boolean = true): Flowable<List<RenterFileData>> {
        var query = "SELECT * FROM files WHERE path LIKE $path/% AND path NOT LIKE $path/%/%"
        if (name != null) {
            query += ""
        }
        if (orderBy != null) { // maybe include secondary sorting
            query += " ORDER BY ${orderBy.text}"
            if (!ascending) {
                query += " DESC"
            }
        }
        return customQuery(query)
    }

    @Query("SELECT * FROM files WHERE path LIKE :path || '/%'")
    abstract fun getFilesUnder(path: String): Single<List<RenterFileData>>

    @Query("DELETE FROM files")
    abstract fun deleteAll()

    @Query("DELETE FROM files WHERE path == :path")
    abstract fun deleteFile(path: String)

    @Query("DELETE FROM files WHERE path LIKE :path || '/%'")
    abstract fun deleteFilesUnder(path: String)
}