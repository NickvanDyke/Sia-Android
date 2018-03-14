package com.vandyke.sia

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.local.daos.getFilesUnderDir
import com.vandyke.sia.data.models.renter.SiaFile
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotContainAny
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test

open class RoomDbDirAndFileDaoTests {
    companion object {
        private val db: AppDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getTargetContext(), AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()

        private const val DIR_1 = "dir1"
        private const val DIR_2 = "dir1/dir2"

        private const val FILE_1 = "file1"
        private const val FILE_2 = "file2"
        private const val FILE_3 = "$DIR_1/file1"
        private const val FILE_4 = "$DIR_1/file3"
        private const val FILE_5 = "$DIR_2/file4"
        private const val FILE_6 = "$DIR_2/file5"

        private val files = listOf(
                SiaFile.withPath(FILE_1),
                SiaFile.withPath(FILE_2),
                SiaFile.withPath(FILE_3),
                SiaFile.withPath(FILE_4),
                SiaFile.withPath(FILE_5),
                SiaFile.withPath(FILE_6))

        @AfterClass
        @JvmStatic
        fun closeDb() {
            db.close()
        }
    }

    @Before
    fun reinitDb() {
        db.clearAllTables()
        db.fileDao().insertAllAbortOnConflict(files)
    }

    @Test
    fun filesUnderDir() {
        db.fileDao().getFilesUnderDir("").blockingGet().size shouldEqual files.size
        db.fileDao().getFilesUnderDir(DIR_1).blockingGet()
                .shouldContainAll(files.filter { it.path.startsWith(DIR_1) })
                .shouldNotContainAny(files.filterNot { it.path.startsWith(DIR_1) })
        db.fileDao().getFilesUnderDir(DIR_2).blockingGet()
                .shouldContainAll(files.filter { it.path.startsWith(DIR_2) })
                .shouldNotContainAny(files.filterNot { it.path.startsWith(DIR_2) })
    }

    @Test
    fun updatePath() {

    }

}