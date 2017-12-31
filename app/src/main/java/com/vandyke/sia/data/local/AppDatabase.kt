/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.vandyke.sia.data.local.daos.*
import com.vandyke.sia.data.local.data.renter.Dir
import com.vandyke.sia.data.local.data.renter.File
import com.vandyke.sia.data.models.consensus.ConsensusData
import com.vandyke.sia.data.models.wallet.AddressData
import com.vandyke.sia.data.models.wallet.ScValueData
import com.vandyke.sia.data.models.wallet.TransactionData
import com.vandyke.sia.data.models.wallet.WalletData

@Database(
        entities = [Dir::class, File::class, WalletData::class, TransactionData::class,
            AddressData::class, ConsensusData::class, ScValueData::class],
        version = 8)
@TypeConverters(com.vandyke.sia.data.local.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    /* renter */
    abstract fun dirDao(): DirDao
    abstract fun fileDao(): FileDao

    /* wallet */
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun addressDao(): AddressDao
    abstract fun scValueDao(): ScValueDao

    /* consensus */
    abstract fun consensusDao(): ConsensusDao
}