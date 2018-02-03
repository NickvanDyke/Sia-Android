/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia

/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.models.wallet.AddressData
import org.amshove.kluent.shouldEqual
import org.junit.After
import org.junit.Before
import org.junit.Test

open class AddressDaoInstrumentedTest {
    private lateinit var db: AppDatabase

    @Before
    fun initDb() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getTargetContext(), AppDatabase::class.java)
                .allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun stuff() {
        val address = "hi"
        db.addressDao().insert(AddressData(address))
        val list = db.addressDao().getAll().blockingGet()
        list.isNotEmpty() shouldEqual true
    }
}