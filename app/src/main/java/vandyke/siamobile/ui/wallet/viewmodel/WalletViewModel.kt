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
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.backend.networking.siaApi
import vandyke.siamobile.backend.networking.sub
import vandyke.siamobile.ui.wallet.model.IWalletModel
import vandyke.siamobile.ui.wallet.model.WalletModelHttp
import vandyke.siamobile.util.toSC

class WalletViewModel : ViewModel() {
    val wallet = MutableLiveData<WalletData>()
    val usd = MutableLiveData<ScPriceData>()
    val consensus = MutableLiveData<ConsensusData>()
    val transactions = MutableLiveData<TransactionsData>()
    val success = MutableLiveData<String>()
    val error = MutableLiveData<SiaError>()

    /* for communicating the seed to the view when a new wallet is created */
    val seed = MutableLiveData<String>()

    var model: IWalletModel = WalletModelHttp()

    /* success and error are immediately set back to null because the view only reacts on
       non-null values of them, and if they're holding a non-null value and the
       view is recreated, then it'll display the success/error even though it shouldn't.
       There might be a better way around that */
    fun setSuccess(msg: String) {
        success.value = msg
        success.value = null
    }

    private val setError: (SiaError) -> Unit = {
        error.value = it
        error.value = null
    }

    fun refresh() {
        refreshWallet()
        refreshTransactions()
        refreshConsensus()
    }

    fun refreshWallet() {
        model.getWallet().sub({ it -> wallet.value = it }, setError)
        siaApi.getScPrice().sub({ it -> usd.value = it }, setError)
    }

    fun refreshTransactions() {
        model.getTransactions().sub({ it -> transactions.value = it }, setError)
    }

    fun refreshConsensus() {
        model.getConsensus().sub({ it -> consensus.value = it }, setError)
    }

    fun unlock(password: String) {
        model.unlock(password).sub({
            setSuccess("Unlocked") // TODO: maybe have cool animations eventually, to indicate locking/unlocking/creating?
            refreshWallet()
        }, {
            if (it.reason == SiaError.Reason.WALLET_SCAN_IN_PROGRESS)
                setSuccess("Blockchain scan in progress, please wait...")
            else
                error.value = it
        })
    }

    fun lock() {
        model.lock().sub({
            setSuccess("Locked")
            refreshWallet()
        }, setError)
    }

    fun create(password: String, force: Boolean, seed: String? = null) {
        if (seed == null) {
            model.init(password, "english", force).sub({ it ->
                setSuccess("Created wallet")
                refreshWallet()
                this.seed.value = it.primaryseed
            }, setError)
        } else {
            model.initSeed(password, "english", seed, force).sub({
                setSuccess("Created wallet")
                refreshWallet()
                this.seed.value = seed
            }, setError)
        }
    }

    fun send(amount: String, destination: String) {
        model.send(amount, destination)
                .sub({
                    setSuccess("Sent ${amount.toSC()} SC to $destination")
                    refreshWallet()
                }, setError)
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        model.changePassword(currentPassword, newPassword).sub({
            setSuccess("Changed password")
        }, setError)
    }

    fun sweep(seed: String) {
        model.sweep("english", seed).sub({
            setSuccess("Scanning blockchain, please wait...")
        }, setError)
    }
}