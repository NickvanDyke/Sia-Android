/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import vandyke.siamobile.backend.data.consensus.ConsensusData
import vandyke.siamobile.backend.data.wallet.ScPriceData
import vandyke.siamobile.backend.data.wallet.TransactionsData
import vandyke.siamobile.backend.data.wallet.WalletData
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.backend.networking.Wallet
import vandyke.siamobile.ui.settings.Prefs
import vandyke.siamobile.ui.wallet.model.IWalletModel
import vandyke.siamobile.ui.wallet.model.WalletModelColdStorage
import vandyke.siamobile.ui.wallet.model.WalletModelHttp
import vandyke.siamobile.util.toSC

class WalletViewModel : ViewModel() {
    /* maybe it would be better to split up the Data classes into multiple, more fine-grained LiveDatas.
       However, I'm deciding not to because that would result in a LOT of LiveDatas and observing. This way the
       observer (the view) has access to all the important information and can choose what to display.
       It's also sort of just aggregating a lot of data into fewer sources */
    val wallet = MutableLiveData<WalletData>()
    val usd = MutableLiveData<ScPriceData>()
    val consensus = MutableLiveData<ConsensusData>()
    val transactions = MutableLiveData<TransactionsData>()
    val success = MutableLiveData<String>()
    val error = MutableLiveData<SiaError>()

    /* for communicating the seed to the view when a new wallet is created */
    val seed = MutableLiveData<String>()

    var model: IWalletModel = if (Prefs.operationMode == "cold_storage") WalletModelColdStorage()
        else WalletModelHttp()
    var cachedMode = Prefs.operationMode

    private val setError: (SiaError) -> Unit = {
        error.value = it
    }

    fun checkMode() {
        if (Prefs.operationMode != cachedMode) {
            cachedMode = Prefs.operationMode
            model = if (cachedMode == "cold_storage") WalletModelColdStorage() else WalletModelHttp()
        }
    }

    fun refresh() {
        refreshWallet()
        refreshTransactions()
        refreshConsensus()
    }

    fun refreshWallet() {
        model.getWallet(SiaCallback({ it -> wallet.value = it }, setError))
        Wallet.scPrice(SiaCallback({ it -> usd.value = it }, setError))
    }

    fun refreshTransactions() {
        model.getTransactions(SiaCallback({ it -> transactions.value = it }, setError))
    }

    fun refreshConsensus() {
        model.getConsensus(SiaCallback({ it -> consensus.value = it }, setError))
    }

    fun unlock(password: String) {
        model.unlock(password, SiaCallback({ ->
            success.value = "Unlocked" // TODO: maybe have cool animations eventually, to indicate locking/unlocking/creating?
            refreshWallet()
        }, {
            if (it.reason == SiaError.Reason.WALLET_SCAN_IN_PROGRESS)
                success.value = "Blockchain scan in progress, please wait..."
            else
                error.value = it
        }))
    }

    fun lock() {
        model.lock(SiaCallback({ ->
            success.value = "Locked"
            refreshWallet()
        }, setError))
    }

    fun create(password: String, force: Boolean, seed: String? = null) {
        if (seed == null) {
            model.init(password, "english", force, SiaCallback({ it ->
                success.value = "Created wallet"
                refreshWallet()
                this.seed.value = it.primaryseed
            }, setError))
        } else {
            model.initSeed(password, "english", seed, force, SiaCallback({ ->
                success.value = "Created wallet"
                refreshWallet()
                this.seed.value = seed
            }, setError))
        }
    }

    fun send(amount: String, destination: String) {
        model.send(amount, destination, SiaCallback({ ->
            success.value = "Sent ${amount.toSC()} SC to $destination"
            refreshWallet()
        }, setError))
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        model.changePassword(currentPassword, newPassword, SiaCallback({ ->
            success.value = "Changed password"
        }, setError))
    }

    fun sweep(seed: String) {
        model.sweep("english", seed, SiaCallback({ ->
            success.value = "Scanning blockchain, please wait..."
        }, setError))
    }
}