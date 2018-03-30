/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import android.arch.paging.LivePagedListBuilder
import android.support.v7.util.DiffUtil
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.models.wallet.AddressData
import com.vandyke.sia.data.models.wallet.TransactionData
import com.vandyke.sia.data.remote.NoWallet
import com.vandyke.sia.data.remote.SiaApi
import com.vandyke.sia.util.diffWith
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
    fun updateAll() = Completable.mergeArray(updateWallet(), updateTransactions(), updateAddresses())!!

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
                        {
                            db.transactionDao().insertAbortOnConflict(it)
                        },
                        {
                            db.transactionDao().delete(it)
                        }
                )
            }
            .toCompletable()

    private fun updateAddresses() = api.walletAddresses()
            .doOnSuccess { db.addressDao().insertAllIgnoreOnConflict(it.addresses.map { AddressData(it) }) }
            .toCompletable()

    /* database flowables to be subscribed to */
    fun wallet() = db.walletDao().mostRecent()

    fun walletMonthHistory() = db.walletDao().allLastMonth()

    val transactions by lazy {
        LivePagedListBuilder(db.transactionDao().allByMostRecent(), 20).build()
    }

    fun addresses() = db.addressDao().all()

    /* singles */
    fun getAddress() = api.walletAddress()
            .doOnSuccess { db.addressDao().insertIgnoreOnConflict(it) }
            .onErrorResumeNext {
                /* fallback to db, but only if the reason for the failure was not due to the absence of a wallet */
                if (it !is NoWallet)
                    db.addressDao().getAddress().onErrorResumeNext(Single.error(it))
                else
                    Single.error(it)
            }!!

    fun getAddresses(): Single<List<AddressData>> = api.walletAddresses()
            .map {
                it.addresses.map { AddressData(it) }
            }
            .doOnSuccess { db.addressDao().insertAllIgnoreOnConflict(it) }
            .onErrorResumeNext {
                /* fallback to db, but only if the reason for the failure was not due to the absence of a wallet */
                if (it !is NoWallet)
                    db.addressDao().getAll().onErrorResumeNext(Single.error(it))
                else
                    Single.error(it)
            }!!

    // chose not to store the seeds in a database for security reasons I guess? Maybe I should
    fun getSeeds(dictionary: String = "english") = api.walletSeeds(dictionary)

    fun unlock(password: String) = api.walletUnlock(password)

    fun lock() = api.walletLock()

    fun init(password: String, dictionary: String, force: Boolean) = api.walletInit(password, dictionary, force)
            .doAfterSuccess {
                clearWalletDb().subscribe()
            }!!

    fun initSeed(password: String, dictionary: String, seed: String, force: Boolean) =
            api.walletInitSeed(password, dictionary, seed, force)
                    .concatWith(clearWalletDb())

    fun send(amount: String, destination: String) = api.walletSiacoins(amount, destination)

    fun changePassword(currentPassword: String, newPassword: String) = api.walletChangePassword(currentPassword, newPassword)

    fun sweep(dictionary: String, seed: String) = api.walletSweepSeed(dictionary, seed)

    private fun clearWalletDb() = Completable.fromCallable {
        db.walletDao().deleteAll()
        db.transactionDao().deleteAll()
        db.addressDao().deleteAll()
    }!!

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<TransactionData> = object : DiffUtil.ItemCallback<TransactionData>() {
            override fun areItemsTheSame(oldItem: TransactionData, newItem: TransactionData): Boolean {
                return oldItem.transactionid == newItem.transactionid
            }

            override fun areContentsTheSame(oldItem: TransactionData?, newItem: TransactionData?): Boolean {
                return oldItem == newItem
            }
        }
    }
}