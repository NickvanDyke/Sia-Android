package com.vandyke.sia.room

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.local.daos.filesInDir
import com.vandyke.sia.data.local.daos.filesUnderDirWithName
import com.vandyke.sia.data.local.daos.getFilesUnderDir
import com.vandyke.sia.data.models.renter.SiaFile
import com.vandyke.sia.data.models.renter.name
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldNotContain
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test

open class RoomDbDirAndFileDaoTests {
    companion object {
        private val db: AppDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getTargetContext(), AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()

        private const val DIR1 = "dir1"
        private const val DIR1_DIR2 = "dir1/dir2"

        private const val FILE1 = "file1"
        private const val FILE2 = "file2"
        private const val DIR1_FILE1 = "$DIR1/file1"
        private const val DIR1_FILE3 = "$DIR1/file3"
        private const val DIR1_DIR2_FILE4 = "$DIR1_DIR2/file4"
        private const val DIR1_DIR2_FILE5 = "$DIR1_DIR2/file5"

        private val files = listOf(
                SiaFile.withPath(FILE1),
                SiaFile.withPath(FILE2),
                SiaFile.withPath(DIR1_FILE1),
                SiaFile.withPath(DIR1_FILE3),
                SiaFile.withPath(DIR1_DIR2_FILE4),
                SiaFile.withPath(DIR1_DIR2_FILE5))

        @AfterClass
        @JvmStatic
        fun closeDb() {
            db.close()
        }
    }

    @Before
    fun resetDb() {
        db.clearAllTables()
        db.fileDao().insertAllAbortOnConflict(files)
    }

    @Test
    fun filesUnderDir() {
        db.fileDao().getFilesUnderDir(DIR1).blockingGet()
                .shouldEqualIgnoreOrder(files.filter { it.path.startsWith(DIR1) })
        db.fileDao().getFilesUnderDir(DIR1_DIR2).blockingGet()
                .shouldEqualIgnoreOrder(files.filter { it.path.startsWith(DIR1_DIR2) })
    }

    @Test
    fun filesUnderDirWithName() {
        db.fileDao().filesUnderDirWithName(DIR1, DIR1_FILE1.name()).blockingFirst()
                .shouldEqualIgnoreOrder(files.filter { it.path.matches("$DIR1_FILE1[^/]*".toRegex()) })
        db.fileDao().filesUnderDirWithName(DIR1, "").blockingFirst()
                .shouldEqualIgnoreOrder(files.filter { it.path.startsWith("$DIR1/") })
    }

    @Test
    fun filesInDir() {
        db.fileDao().filesInDir(DIR1).blockingFirst()
                .shouldEqualIgnoreOrder(files.filter { it.path.matches("$DIR1/[^/]*".toRegex()) })
    }

    @Test
    fun updatePath() {
        val currPath1 = DIR1_DIR2_FILE5
        val currPath2 = DIR1_FILE1
        val currPath3 = FILE1
        val newPath1 = "hello/yep"
        val newPath2 = "rootfile"
        val newPath3 = "test/ing/values/wheeee"
        db.fileDao().updatePath(currPath1, newPath1)
        db.fileDao().updatePath(currPath2, newPath2)
        db.fileDao().updatePath(currPath3, newPath3)
        db.fileDao().getAllByPath().blockingGet().paths()
                .shouldContain(newPath1)
                .shouldContain(newPath2)
                .shouldContain(newPath3)
                .shouldNotContain(currPath1)
                .shouldNotContain(currPath2)
                .shouldNotContain(currPath3)
                .shouldContainAll(files.paths().filterNot { it == currPath1 || it == currPath2 || it == currPath3 })
    }

    private fun List<SiaFile>.paths() = this.map(SiaFile::path)
}