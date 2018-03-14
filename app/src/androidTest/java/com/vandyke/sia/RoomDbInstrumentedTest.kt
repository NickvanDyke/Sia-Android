/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia

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

open class RoomDbInstrumentedTest {
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

    /* below, the BaseDao methods are tested. We use AddressDao because it's the simplest implementation of BaseDao */
    @Test
    fun insertAndGetAll() {
        val addressData = AddressData("hi")
        db.addressDao().insertAbortOnConflict(addressData)
        val list = db.addressDao().getAll().blockingGet()
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
    fun delete() {
        val addressData = AddressData("hi")
        db.addressDao().insertAbortOnConflict(addressData)
        db.addressDao().delete(addressData)
        db.addressDao().getAll().blockingGet().shouldBeEmpty()
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