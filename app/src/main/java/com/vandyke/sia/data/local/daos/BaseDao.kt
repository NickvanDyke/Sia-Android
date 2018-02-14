package com.vandyke.sia.data.local.daos

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy

interface BaseDao<in T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplaceOnConflict(item: T)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAbortOnConflict(item: T)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnoreOnConflict(item: T)

    @Insert
    fun insertAllIgnoreOnConflict(items: List<T>)

    @Delete
    fun delete(item: T)
}
