/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.*
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.repository.FilesRepository
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
abstract class DirDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertReplaceIfConflict(dir: Dir)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertAbortIfConflict(dir: Dir)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertIgnoreIfConflict(dir: Dir)

    @Query("UPDATE dirs SET path = :newPath WHERE path == :path")
    abstract fun updatePath(path: String, newPath: String)

    @Query("SELECT * FROM dirs")
    abstract fun all(): Flowable<List<Dir>>

    @Query("SELECT * FROM dirs")
    abstract fun getAll(): Single<List<Dir>>

    @Query("SELECT * FROM dirs WHERE path = :path")
    abstract fun dir(path: String): Flowable<Dir>

    // TODO: should maybe return a Maybe instead of a Single? Since I don't want an error when it's an empty result
    @Query("SELECT * FROM dirs WHERE path = :path")
    abstract fun getDir(path: String): Single<Dir>

    @RawQuery(observedEntities = [Dir::class])
    abstract fun customQuery(query: String): Flowable<List<Dir>>

    fun getDirs(path: String, name: String? = null, orderBy: FilesRepository.OrderBy? = null, ascending: Boolean = true): Flowable<List<Dir>> {
        var query = "SELECT * FROM dirs WHERE path LIKE $path/% AND path NOT LIKE $path/%/%"
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

    @Query("DELETE FROM dirs")
    abstract fun deleteAll()

    @Query("DELETE FROM dirs WHERE path = :path")
    abstract fun deleteDir(path: String)

    @Query("DELETE FROM dirs WHERE path LIKE :path || '/%'")
    abstract fun deleteDirsUnder(path: String)
}