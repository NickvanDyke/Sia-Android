/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vandyke.sia.data.local.daos.*
import com.vandyke.sia.data.models.consensus.ConsensusData
import com.vandyke.sia.data.models.renter.*
import com.vandyke.sia.data.models.wallet.*

@Database(
        entities = [
            Dir::class,
            SiaFile::class,
            WalletData::class,
            TransactionData::class,
            AddressData::class,
            SeedData::class,
            ConsensusData::class,
            ScValueData::class,
            PricesData::class,
            RenterFinancialMetricsData::class,
            RenterSettingsAllowanceData::class,
            CurrentPeriodData::class,
            ContractData::class
        ],
        version = 44)
@TypeConverters(com.vandyke.sia.data.local.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    /* renter */
    abstract fun dirDao(): DirDao
    abstract fun fileDao(): FileDao
    abstract fun pricesDao(): PricesDao
    abstract fun spendingDao(): SpendingDao
    abstract fun allowanceDao(): AllowanceDao
    abstract fun currentPeriodDao(): CurrentPeriodDao
    abstract fun contractDao(): ContractDao

    /* wallet */
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun addressDao(): AddressDao
    abstract fun seedDao(): SeedDao
    abstract fun scValueDao(): ScValueDao

    /* consensus */
    abstract fun consensusDao(): ConsensusDao
}