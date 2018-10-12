/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import androidx.paging.LivePagedListBuilder
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.models.wallet.AddressData
import com.vandyke.sia.data.models.wallet.SeedData
import com.vandyke.sia.data.models.wallet.TransactionData
import com.vandyke.sia.data.models.wallet.WalletInitData
import com.vandyke.sia.data.remote.NoWallet
import com.vandyke.sia.data.remote.SiaApi
import com.vandyke.sia.data.remote.WalletLocked
import com.vandyke.sia.util.diffWith
import com.vandyke.sia.util.rx.inDbTransaction
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository
@Inject constructor(
        private val api: SiaApi,
        private val db: AppDatabase
) {
    /* Functions that update the local database from the Sia node */
    fun updateAll() = Completable.mergeArray(updateWallet(), updateTransactions())!!

    private fun updateWallet() = api.wallet()
            .doOnSuccess { db.walletDao().insertReplaceOnConflict(it) }
            .toCompletable()

    private fun updateTransactions() = api.walletTransactions(0, Int.MAX_VALUE)
            .map { it.alltransactions.sortedBy(TransactionData::transactionid) }
            .zipWith(db.transactionDao().getAllById())
            .doOnSuccess { (apiTxs, dbTxs) ->
                apiTxs.diffWith(
                        dbTxs,
                        TransactionData::transactionid,
                        { apiTx, dbTx ->
                            if (apiTx != dbTx)
                                db.transactionDao().insertReplaceOnConflict(apiTx)
                        },
                        { db.transactionDao().insertAbortOnConflict(it) },
                        { db.transactionDao().delete(it) })
            }
            .toCompletable()
            .inDbTransaction(db)

    /* database flowables to be subscribed to */
    fun wallet() = db.walletDao().mostRecent()

    fun walletMonthHistory() = db.walletDao().allLastMonth()

    val transactions by lazy {
        LivePagedListBuilder(db.transactionDao().allByMostRecent(), 30).build()
    }

    /* singles */
    fun getAddress() = api.walletAddress()
            .doOnSuccess { db.addressDao().insertIgnoreOnConflict(it) }
            .onErrorResumeNext {
                /* fallback to db, but only if the reason for the failure was not due to the absence of a wallet */
                if (it !is NoWallet)
                    db.addressDao().getAddress()
                            .onErrorResumeNext(Single.error(it))
                else
                    Single.error(it)
            }!!

    fun getAddresses(): Single<List<AddressData>> = api.walletAddresses()
            .map {
                it.addresses.map { AddressData(it) }
            }
            .doOnSuccess {
                /* easier and faster to just do this. And nothing is subscribed to flowables from this
                 * table so it doesn't matter if we do it this way */
                db.addressDao().deleteAll()
                db.addressDao().insertAllAbortOnConflict(it)
            }
            .onErrorResumeNext { t ->
                /* fallback to db, but only if the reason for the failure was not due to the absence of a wallet */
                if (t !is NoWallet)
                    db.addressDao().getAll()
                            .doOnSuccess { if (it.isEmpty()) throw t }
                            .onErrorResumeNext(Single.error(t))
                else
                    Single.error(t)
            }

    fun getSeeds(dictionary: String): Single<List<SeedData>> = api.walletSeeds(dictionary)
            .map {
                it.allseeds.map { SeedData(it) }
            }
            .doOnSuccess {
                db.seedDao().deleteAll()
                db.seedDao().insertAllAbortOnConflict(it)
            }
            .onErrorResumeNext { t ->
                if (t !is NoWallet && t !is WalletLocked)
                    db.seedDao().getAll()
                            .doOnSuccess { if (it.isEmpty()) throw t }
                            .onErrorResumeNext(Single.error(t))
                else
                    Single.error(t)
            }

    fun unlock(password: String) = api.walletUnlock(password)

    fun lock() = api.walletLock()

    fun init(password: String, dictionary: String, force: Boolean): Single<WalletInitData> = api.walletInit(password, dictionary, force)
            .doOnSuccess {
                clearWalletDb()
                db.seedDao().insertAbortOnConflict(SeedData(it.primaryseed))
            }

    fun initSeed(password: String, dictionary: String, seed: String, force: Boolean): Completable =
            api.walletInitSeed(password, dictionary, seed, force)
                    .concatWith(Completable.fromAction { clearWalletDb() })
                    .doOnComplete { db.seedDao().insertAbortOnConflict(SeedData(seed)) }

    fun send(amount: String, destination: String) = api.walletSiacoins(amount, destination)

    fun changePassword(currentPassword: String, newPassword: String) = api.walletChangePassword(currentPassword, newPassword)

    fun sweep(dictionary: String, seed: String) = api.walletSweepSeed(dictionary, seed)

    private fun clearWalletDb() {
        db.walletDao().deleteAll()
        db.seedDao().deleteAll()
        db.transactionDao().deleteAll()
        db.addressDao().deleteAll()
    }
}