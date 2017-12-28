/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import vandyke.siamobile.data.local.daos.*
import vandyke.siamobile.data.local.data.renter.Dir
import vandyke.siamobile.data.local.data.renter.File
import vandyke.siamobile.data.models.consensus.ConsensusData
import vandyke.siamobile.data.models.wallet.AddressData
import vandyke.siamobile.data.models.wallet.ScValueData
import vandyke.siamobile.data.models.wallet.TransactionData
import vandyke.siamobile.data.models.wallet.WalletData

@Database(
        entities = [Dir::class, File::class, WalletData::class, TransactionData::class,
            AddressData::class, ConsensusData::class, ScValueData::class],
        version = 8)
@TypeConverters(vandyke.siamobile.data.local.TypeConverters::class)
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