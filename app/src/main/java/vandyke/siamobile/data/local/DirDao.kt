/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.intellij.lang.annotations.Language

@Dao
interface DirDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnoreConflict(vararg dirs: Dir)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAbortIfConflict(vararg dirs: Dir)

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