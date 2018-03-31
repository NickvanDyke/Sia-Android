/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.room

/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

import android.arch.persistence.room.Room
import android.database.sqlite.SQLiteConstraintException
import android.support.test.InstrumentationRegistry
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.models.renter.PricesData
import com.vandyke.sia.data.models.wallet.AddressData
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldEqual
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

open class RoomDbBasicTests {
    companion object {
        private val db: AppDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getTargetContext(), AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()

        @AfterClass
        @JvmStatic
        fun closeDb() {
            db.close()
        }
    }

    /* clear the db before each test */
    @Before
    fun clearDb() {
        db.clearAllTables()
    }

    /* Many Dao methods are used in multiple Daos. Their implementation is always the same, just with
     * different table names, so the below tests just choose one of the many Daos to use to test the particular method. */
    @Test
    fun insert() {
        val addressData = AddressData("hi")
        db.addressDao().insertAbortOnConflict(addressData)
        val list = db.addressDao().getAllSorted().blockingGet()
        list.size shouldEqual 1
        list[0] shouldEqual addressData
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertAbortOnConflict() {
        val addressData = AddressData("hi")
        db.addressDao().insertAbortOnConflict(addressData)
        db.addressDao().insertAbortOnConflict(addressData)
    }

    @Test
    fun insertIgnoreOnConflict() {
        val addressData = AddressData("hi")
        db.addressDao().insertAbortOnConflict(addressData)
        db.addressDao().insertIgnoreOnConflict(addressData)
    }

    @Test
    fun insertAll() {
        val list = listOf(
                AddressData("one"),
                AddressData("two"),
                AddressData("three"))
        db.addressDao().insertAllAbortOnConflict(list)
        val fromDb = db.addressDao().getAllSorted().blockingGet()
        fromDb.size shouldEqual list.size
        fromDb shouldEqual list
    }

    @Test
    fun delete() {
        val addressData = AddressData("hi")
        db.addressDao().insertAbortOnConflict(addressData)
        db.addressDao().delete(addressData)
        db.addressDao().getAllSorted().blockingGet().shouldBeEmpty()
    }

    @Test
    fun deleteAll() {
        db.addressDao().insertAbortOnConflict(AddressData("hi"))
        db.addressDao().insertAbortOnConflict(AddressData("hello"))
        db.addressDao().insertAbortOnConflict(AddressData("whats up"))
        db.addressDao().deleteAll()
        db.addressDao().getAllSorted().blockingGet().shouldBeEmpty()
    }

    @Test
    fun mostRecent() {
        val new = PricesData(100L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
        val middle = PricesData(75L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
        val old = PricesData(50L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
        db.pricesDao().insertAbortOnConflict(middle)
        db.pricesDao().insertAbortOnConflict(new)
        db.pricesDao().insertAbortOnConflict(old)
        db.pricesDao().mostRecent().blockingFirst().timestamp shouldEqual new.timestamp
    }
}