/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.SiaError
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.models.wallet.AddressData
import com.vandyke.sia.data.remote.SiaApiInterface
import io.reactivex.Completable
import io.reactivex.Single

class WalletRepository(val api: SiaApiInterface, val db: AppDatabase) {

    /* Functions that update the local database from the Sia node */
    fun updateAll() = Completable.mergeArray(updateWallet(), updateTransactions(), updateAddresses())!!

    private fun updateWallet() = api.wallet().doOnSuccess {
        db.walletDao().insert(it)
    }.toCompletable()

    private fun updateTransactions() = api.walletTransactions().doOnSuccess {
        db.transactionDao().deleteAllAndInsert(it.alltransactions) // TODO: more efficient way?
    }.toCompletable()

    private fun updateAddresses() = api.walletAddresses().doOnSuccess {
        db.addressDao().insertAll(it.addresses.map { AddressData(it) })
    }.toCompletable()

    /* database flowables to be subscribed to */
    fun wallet() = db.walletDao().mostRecent()

    fun transactions() = db.transactionDao().allByMostRecent()

    fun addresses() = db.addressDao().all()

    /* singles */
    fun getAddress() = api.walletAddress().doOnSuccess {
        db.addressDao().insert(it)
    }.onErrorResumeNext {
        // fallback to DB
        val siaError = SiaError(it)
        // don't attempt to get an address from the db if the reason the request to the API failed is that it doesn't have a wallet
        if (siaError.reason != SiaError.Reason.WALLET_NOT_ENCRYPTED)
            db.addressDao().getAddress().onErrorResumeNext(Single.error(siaError))
        else
            Single.error(siaError)
    }!!

    fun getAddresses(): Single<List<AddressData>> = api.walletAddresses().map {
        it.addresses.map { AddressData(it) }
    }.doOnSuccess {
        db.addressDao().insertAll(it)
    }.onErrorResumeNext {
        val siaError = SiaError(it)
        if (siaError.reason != SiaError.Reason.WALLET_NOT_ENCRYPTED)
            db.addressDao().getAll().onErrorResumeNext(Single.error(siaError))
        else
            Single.error(siaError)
    }!!

    // chose not to store the seeds in a database for security reasons I guess? Maybe I should
    fun getSeeds(dictionary: String = "english") = api.walletSeeds(dictionary)

    /* Below are actions that affect the Sia node. Previously, it would call the appropriate updates upon completion of the actions.
     * I changed it because then errors with updating will propagate up these observables, and these observables won't
     * complete until the updates they call also complete, which isn't the behavior I want when using them. Is there a way
     * to subscribe to the update observables within these observables without that behavior? */
    fun unlock(password: String) = api.walletUnlock(password)

    fun lock() = api.walletLock()

    fun init(password: String, dictionary: String, force: Boolean) = api.walletInit(password, dictionary, force).doAfterSuccess {
        clearWalletDb().subscribe()
    }!!

    fun initSeed(password: String, dictionary: String, seed: String, force: Boolean) = api.walletInitSeed(password, dictionary, seed, force)
            .doOnComplete {
                clearWalletDb().subscribe()
            }!!

    fun send(amount: String, destination: String) = api.walletSiacoins(amount, destination)

    fun changePassword(currentPassword: String, newPassword: String) = api.walletChangePassword(currentPassword, newPassword)

    fun sweep(dictionary: String, seed: String) = api.walletSweepSeed(dictionary, seed)

    fun clearWalletDb() = Completable.fromCallable {
        db.walletDao().deleteAll()
        db.transactionDao().deleteAll()
        db.addressDao().deleteAll()
    }!!
}