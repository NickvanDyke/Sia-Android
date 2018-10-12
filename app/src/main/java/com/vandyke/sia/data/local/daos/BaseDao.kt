package com.vandyke.sia.data.local.daos

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy

interface BaseDao<in T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplaceOnConflict(item: T)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAbortOnConflict(item: T)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnoreOnConflict(item: T)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllIgnoreOnConflict(items: List<T>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAllAbortOnConflict(items: List<T>)

    @Delete
    fun delete(item: T)
}
