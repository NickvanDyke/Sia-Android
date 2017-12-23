package vandyke.siamobile.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [Dir::class, File::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dirDao(): DirDao
    abstract fun fileDao(): FileDao
}