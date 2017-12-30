/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.repository

import com.vandyke.siamobile.data.models.wallet.AddressData
import com.vandyke.siamobile.data.remote.SiaError
import com.vandyke.siamobile.data.remote.siaApi
import com.vandyke.siamobile.db
import io.reactivex.Completable
import io.reactivex.Single

class WalletRepository {

    /* Functions that update the local database from the Sia node */
    fun updateAllWalletStuff() = Completable.mergeArray(updateWallet(), updateTransactions(), updateAddresses())!!

    private fun updateWallet() = siaApi.wallet().doAfterSuccess {
        db.walletDao().insert(it)
    }.toCompletable()

    private fun updateTransactions() = siaApi.walletTransactions().doAfterSuccess {
        db.transactionDao().deleteAllAndInsert(it.alltransactions) // TODO: more efficient way?
    }.toCompletable()

    private fun updateAddresses() = siaApi.walletAddresses().doAfterSuccess {
        db.addressDao().insertAll(it.addresses.map { AddressData(it) })
    }.toCompletable()

    /* functions that return database flowables to be subscribed to */
    fun wallet() = db.walletDao().mostRecent()

    fun transactions() = db.transactionDao().allByMostRecent()

    fun getAddress() = siaApi.walletAddress().doAfterSuccess {
        db.addressDao().insert(it)
    }.onErrorResumeNext {
        // don't attempt to get an address from the db if the reason the request to the API failed is that it doesn't have a wallet
        if (it is SiaError && it.reason != SiaError.Reason.WALLET_NOT_ENCRYPTED)
            db.addressDao().getAddress()
        else
            Single.error(it)
    }

    fun addresses() = db.addressDao().all()

    // chose not to store the seeds in a database for security reasons I guess? Maybe I should
    fun getSeeds(dictionary: String = "english") = siaApi.walletSeeds(dictionary)

    /* Below are actions that affect the Sia node. The appropriate updates will be called upon completion of the actions. */
    fun unlock(password: String) = siaApi.walletUnlock(password).doOnComplete {
        updateWallet().subscribe()
    }!!

    fun lock() = siaApi.walletLock().doOnComplete {
        updateWallet().subscribe()
    }!!

    fun init(password: String, dictionary: String, force: Boolean)
            = siaApi.walletInit(password, dictionary, force).doAfterSuccess {
        clearWalletDb().subscribe()
        updateWallet().subscribe()
        updateTransactions().subscribe()
    }!!

    fun initSeed(password: String, dictionary: String, seed: String, force: Boolean)
            = siaApi.walletInitSeed(password, dictionary, seed, force).doOnComplete {
        clearWalletDb().subscribe()
        updateWallet().subscribe()
        updateTransactions().subscribe()
    }!!

    fun send(amount: String, destination: String) = siaApi.walletSiacoins(amount, destination).doOnComplete {
        updateWallet().subscribe()
        updateTransactions().subscribe()
    }!!

    fun changePassword(currentPassword: String, newPassword: String) = siaApi.walletChangePassword(currentPassword, newPassword)

    fun sweep(dictionary: String, seed: String) = siaApi.walletSweepSeed(dictionary, seed).doOnComplete {
        updateWallet().subscribe()
        updateTransactions().subscribe()
    }!!

    fun clearWalletDb() = Completable.create {
        db.walletDao().deleteAll()
        db.transactionDao().deleteAll()
        db.addressDao().deleteAll()
        it.onComplete()
    }!!
}