/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import vandyke.siamobile.data.local.data.Dir
import vandyke.siamobile.data.local.data.File

@Database(entities = [Dir::class, File::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dirDao(): DirDao
    abstract fun fileDao(): FileDao
}