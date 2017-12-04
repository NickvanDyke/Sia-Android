///*
// * Copyright (c) 2017 Nicholas van Dyke
// *
// * This file is subject to the terms and conditions defined in 'LICENSE.md'
// */
//
//package vandyke.siamobile.ui.wallet.model
//
//import android.content.Context
//import android.support.v7.app.AlertDialog
//import io.reactivex.Observable
//import siawallet.Wallet
//import vandyke.siamobile.backend.data.consensus.ConsensusData
//import vandyke.siamobile.backend.data.explorer.ExplorerTransactionData
//import vandyke.siamobile.backend.data.wallet.*
//import vandyke.siamobile.backend.networking.Explorer
//import vandyke.siamobile.backend.networking.SiaCallback
//import vandyke.siamobile.backend.networking.SiaError
//import vandyke.siamobile.ui.settings.Prefs
//import vandyke.siamobile.util.SCUtil
//import java.math.BigDecimal
//import java.util.concurrent.atomic.AtomicInteger
//
//class WalletModelColdStorage {
//    private var seed: String = Prefs.coldStorageSeed
//    private var addresses: List<String> = Prefs.coldStorageAddresses.toList()
//    private var password: String = Prefs.coldStoragePassword
//    private var exists: Boolean = Prefs.coldStorageExists
//    private var unlocked: Boolean = false
//
//    override fun wallet(callback: SiaCallback<WalletData>) {
//        val counter = AtomicInteger(addresses.size)
//        var balance = BigDecimal.ZERO
//        if (!exists) {
//            callback.onSuccess?.invoke(WalletData(exists, unlocked, false, BigDecimal.ZERO))
//            return
//        }
//        for (address in addresses) {
//            Explorer.siaTechHash(address, SiaCallback({ it ->
//                it.transactions.map { it.toTransactionData() }.forEach { balance += it.netValue }
//                if (counter.decrementAndGet() == 0)
//                    callback.onSuccess?.invoke(WalletData(exists, unlocked, false, balance))
//            }, {
//                if (it.reason == SiaError.Reason.UNRECOGNIZED_HASH) {
//                    if (counter.decrementAndGet() == 0)
//                        callback.onSuccess?.invoke(WalletData(exists, unlocked, false, balance))
//                } else {
//                    callback.onError(it)
//                }
//            }))
//        }
//    }
//
//    override fun walletAddress(callback: SiaCallback<AddressData>) {
//        when {
//            !exists -> callback.onError(SiaError(SiaError.Reason.WALLET_NOT_ENCRYPTED))
//            !unlocked -> callback.onError(SiaError(SiaError.Reason.WALLET_LOCKED))
//            else -> callback.onSuccess?.invoke(AddressData(addresses[(Math.random() * addresses.size).toInt()]))
//        }
//    }
//
//    override fun walletAddresses(callback: SiaCallback<AddressesData>) {
//        when {
//            !exists -> callback.onError(SiaError(SiaError.Reason.WALLET_NOT_ENCRYPTED))
//            !unlocked -> callback.onError(SiaError(SiaError.Reason.WALLET_LOCKED))
//            else -> callback.onSuccess?.invoke(AddressesData(addresses))
//        }
//    }
//
//    override fun walletTransactions(callback: SiaCallback<TransactionsData>) {
//        val counter = AtomicInteger(addresses.size)
//        val txs = mutableListOf<TransactionData>()
//        for (address in addresses) {
//            Explorer.siaTechHash(address, SiaCallback({ it ->
//                txs += it.transactions.map { it.toTransactionData() }
//                if (counter.decrementAndGet() == 0)
//                    callback.onSuccess?.invoke(TransactionsData(txs.sortedBy { it.confirmationtimestamp }))
//            }, {
//                if (it.reason == SiaError.Reason.UNRECOGNIZED_HASH) {
//                    if (counter.decrementAndGet() == 0)
//                        callback.onSuccess?.invoke(TransactionsData(txs.sortedBy { it.confirmationtimestamp }))
//                } else {
//                    callback.onError(it)
//                }
//            }))
//        }
//    }
//
//    override fun walletSeeds(dictionary: String, callback: SiaCallback<SeedsData>) {
//        when {
//            !exists -> callback.onError(SiaError(SiaError.Reason.WALLET_NOT_ENCRYPTED))
//            !unlocked -> callback.onError(SiaError(SiaError.Reason.WALLET_LOCKED))
//            else -> callback.onSuccess?.invoke(SeedsData(seed))
//        }
//    }
//
//    override fun consensus(callback: SiaCallback<ConsensusData>) {
//        Explorer.siaTech(SiaCallback({ it ->
//            callback.onSuccess?.invoke(ConsensusData(true, it.height))
//        }, {
//            callback.onError(it)
//        }))
//    }
//
//    override fun unlock(password: String, callback: SiaCallback<Unit>) {
//        when {
//            !exists -> callback.onError(SiaError(SiaError.Reason.WALLET_NOT_ENCRYPTED))
//            password == this.password -> {
//                unlocked = true
//                callback.onSuccessNull?.invoke()
//            }
//            else -> callback.onError(SiaError(SiaError.Reason.WALLET_PASSWORD_INCORRECT))
//        }
//    }
//
//    override fun lock(callback: SiaCallback<Unit>) {
//        when {
//            !exists -> callback.onError(SiaError(SiaError.Reason.WALLET_NOT_ENCRYPTED))
//            !unlocked -> callback.onError(SiaError(SiaError.Reason.WALLET_LOCKED))
//            else -> {
//                unlocked = false
//                callback.onSuccessNull?.invoke()
//            }
//        }
//    }
//
//    override fun init(password: String, dictionary: String, force: Boolean, callback: SiaCallback<WalletInitData>) {
//        if (exists && !force) {
//            callback.onError(SiaError(SiaError.Reason.EXISTING_WALLET))
//            return
//        }
//
//        val wallet = Wallet()
//        try {
//            wallet.generateSeed()
//            this.seed = wallet.seed
//        } catch (e: Exception) {
//            e.printStackTrace()
//            this.seed = "Failed to generate seed"
//        }
//
//        addresses = List(5, { wallet.walletAddress(it.toLong()) })
//
//        this.password = password
//        exists = true
//        unlocked = false
//        Prefs.coldStorageSeed = this.seed
//        Prefs.coldStorageAddresses.clear()
//        Prefs.coldStorageAddresses.addAll(addresses)
//        Prefs.coldStoragePassword = password
//        Prefs.coldStorageExists = exists
//        callback.onSuccess?.invoke(WalletInitData(seed))
//    }
//
//    override fun initSeed(password: String, dictionary: String, seed: String, force: Boolean, callback: SiaCallback<Unit>) {
//        callback.onError(SiaError(SiaError.Reason.UNSUPPORTED_ON_COLD_WALLET))
//    }
//
//    override fun send(amount: String, destination: String) = Observable.error<Unit>(SiaError(SiaError.Reason.UNSUPPORTED_ON_COLD_WALLET))
//
//    override fun changePassword(currentPassword: String, newPassword: String, callback: SiaCallback<Unit>) {
//        callback.onError(SiaError(SiaError.Reason.UNSUPPORTED_ON_COLD_WALLET))
//    }
//
//    override fun sweep(dictionary: String, seed: String, callback: SiaCallback<Unit>) {
//        callback.onError(SiaError(SiaError.Reason.UNSUPPORTED_ON_COLD_WALLET))
//    }
//
//    fun ExplorerTransactionData.toTransactionData(): TransactionData {
//        return TransactionData(id, BigDecimal(height), BigDecimal(SCUtil.estimatedTimeAtBlock(height)),
//                siacoininputoutputs.map { TransactionInputData(walletaddress = addresses.contains(it.unlockhash), value = it.value) }, // inputs
//                rawtransaction.siacoinoutputs.map { TransactionOutputData(walletaddress = addresses.contains(it.unlockhash), value = it.value) }) // outputs
//    }
//
//    companion object {
//        fun showColdStorageHelp(context: Context) {
//            AlertDialog.Builder(context)
//                    .setTitle("Cold storage help")
//                    .setMessage("Sia Mobile's cold storage wallet operates independently of the Sia network." +
//                            " Since it doesn't have a copy of the Sia blockchain and is not connected to the " +
//                            "Sia network, it cannot perform certain functions that require this. It will still attempt to ESTIMATE your CONFIRMED balance and transactions" +
//                            " using explore.sia.tech." +
//                            "\n\nIf you wish to use unsupported functions, or view your cold wallet balance and transactions, you can, at any time, run a full" +
//                            " Sia node (either in Sia Mobile or using Sia-UI on your computer), and then load your" +
//                            " wallet seed on that full node.")
//                    .setPositiveButton("OK", null)
//                    .show()
//        }
//    }
//}