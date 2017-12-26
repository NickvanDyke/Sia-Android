/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.repository

import io.reactivex.Completable
import vandyke.siamobile.data.local.data.wallet.Address
import vandyke.siamobile.data.local.data.wallet.Transaction
import vandyke.siamobile.data.local.data.wallet.Wallet
import vandyke.siamobile.data.remote.siaApi
import vandyke.siamobile.db

class WalletRepository {

    /* Functions that update the local database from the Sia node */
    fun updateAllWalletStuff(): Completable {
        return Completable.mergeArray(updateWallet(), updateTransactions(), updateAddresses())
    }

    private fun updateWallet() = siaApi.wallet().doAfterSuccess {
        db.walletDao().insert(Wallet.fromWalletData(it))
    }.toCompletable()

    private fun updateTransactions() = siaApi.walletTransactions().doAfterSuccess {
        val mapped = it.alltransactions.map { Transaction.fromTransactionData(it) }
        db.transactionDao().deleteAllAndInsert(mapped)
    }.toCompletable()

    private fun updateAddresses() = siaApi.walletAddresses().doAfterSuccess {
        db.addressDao().insertAll(Address.fromAddressesData(it))
    }.toCompletable()

    /* functions that return database flowables to be subscribed to */
    fun getWallet() = db.walletDao().getMostRecent()

    fun getTransactions() = db.transactionDao().getAllByMostRecent()

    fun getAddress() = db.addressDao().getAddress()

    fun getAddresses() = db.addressDao().getAll()

    fun getSeeds(dictionary: String = "english") = siaApi.walletSeeds(dictionary)

    /* Below are actions that affect the Sia node. The appropriate updates will be called upon completion of the actions. */
    fun unlock(password: String) = siaApi.walletUnlock(password).doOnComplete {
        updateWallet().subscribe()
    }

    fun lock() = siaApi.walletLock().doOnComplete {
        updateWallet().subscribe()
    }

    fun init(password: String, dictionary: String, force: Boolean)
            = siaApi.walletInit(password, dictionary, force).doAfterSuccess {
        updateWallet().subscribe()
        updateTransactions().subscribe()
    }

    fun initSeed(password: String, dictionary: String, seed: String, force: Boolean)
            = siaApi.walletInitSeed(password, dictionary, seed, force).doOnComplete {
        updateWallet().subscribe()
        updateTransactions().subscribe()
    }

    fun send(amount: String, destination: String) = siaApi.walletSiacoins(amount, destination).doOnComplete {
        updateWallet().subscribe()
        updateTransactions().subscribe()
    }

    fun changePassword(currentPassword: String, newPassword: String) = siaApi.walletChangePassword(currentPassword, newPassword)

    fun sweep(dictionary: String, seed: String) = siaApi.walletSweepSeed(dictionary, seed).doOnComplete {
        updateWallet().subscribe()
        updateTransactions().subscribe()
    }
}