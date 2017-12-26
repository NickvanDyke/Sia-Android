/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.wallet.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import vandyke.siamobile.data.remote.SiaError
import vandyke.siamobile.data.remote.data.consensus.ConsensusData
import vandyke.siamobile.data.remote.data.wallet.*
import vandyke.siamobile.data.remote.siaApi
import vandyke.siamobile.data.siad.SiadService
import vandyke.siamobile.ui.wallet.model.IWalletModel
import vandyke.siamobile.ui.wallet.model.WalletModelHttp
import vandyke.siamobile.util.siaSubscribe
import vandyke.siamobile.util.toSC

class WalletViewModel : ViewModel() {
    val wallet = MutableLiveData<WalletData>()
    val usd = MutableLiveData<ScPriceData>()
    val consensus = MutableLiveData<ConsensusData>()
    val transactions = MutableLiveData<TransactionsData>()
    val activeTasks = MutableLiveData<Int>()
    val numPeers = MutableLiveData<Int>()
    val success = MutableLiveData<String>()
    val error = MutableLiveData<SiaError>()

    /* seed is used specifically for when a new wallet is created - not for when called /wallet/seeds */
    val seed = MutableLiveData<String>()

    /* when you see a LiveData's value being set to null immediately after, it's so that an observer
       won't receive anything upon initial subscription to it (normally it still would, but generally
       an extension function is used that doesn't pass the value unless it's not null) */
    /* the below LiveDatas are used by child fragments of the Wallet page */
    val address = MutableLiveData<AddressData>()
    val addresses = MutableLiveData<AddressesData>()
    val seeds = MutableLiveData<SeedsData>()

    val model: IWalletModel = WalletModelHttp()

    private val subscription = SiadService.output.observeOn(AndroidSchedulers.mainThread()).subscribe {
        if (it.contains("Finished loading") || it.contains("Done!"))
            refresh()
    }

    init {
        activeTasks.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    /* success and error are immediately set back to null because the view only reacts on
       non-null values of them, and if they're holding a non-null value and the
       view is recreated, then it'll display the success/error even though it shouldn't.
       There might be a better way around that */
    private fun setSuccess(msg: String) {
        decrementTasks()
        success.value = msg
        success.value = null
    }
    
    private fun onError(err: SiaError) {
        decrementTasks()
        error.value = err
        error.value = null
    }

    private fun incrementTasks() {
        activeTasks.value = activeTasks.value!! + 1
    }

    private fun decrementTasks() {
        if (activeTasks.value!! > 0)
            activeTasks.value = activeTasks.value!! - 1
    }

    fun refresh() {
        refreshWallet()
        refreshTransactions()
        refreshConsensus()
        refreshPeers()
    }

    fun refreshWallet() {
        incrementTasks()
        model.getWallet().siaSubscribe({
            wallet.value = it
            decrementTasks()
        }, ::onError)
        /* we don't track retrieving the usd price as a task since it tends to take a long time and is less reliable */
        siaApi.getScPrice().siaSubscribe({
            usd.value = it
        }, {
            error.value = it
            error.value = null
        })
    }

    fun refreshTransactions() {
        incrementTasks()
        model.getTransactions().siaSubscribe({
            transactions.value = it
            decrementTasks()
        }, ::onError)
    }

    fun refreshConsensus() {
        incrementTasks()
        model.getConsensus().siaSubscribe({
            consensus.value = it
            decrementTasks()
        }, ::onError)
    }

    fun refreshPeers() {
        incrementTasks()
        siaApi.gateway().siaSubscribe({
            numPeers.value = it.peers.size
            decrementTasks()
        }, ::onError)
    }

    fun unlock(password: String) {
        incrementTasks()
        model.unlock(password).siaSubscribe({
            setSuccess("Unlocked") // TODO: maybe have cool animations eventually, to indicate locking/unlocking/creating?
            refreshWallet()
        }, {
            if (it.reason == SiaError.Reason.WALLET_SCAN_IN_PROGRESS)
                setSuccess("Blockchain scan in progress, please wait...")
            else
                onError(it)
        })
    }

    fun lock() {
        incrementTasks()
        model.lock().siaSubscribe({
            setSuccess("Locked")
            refreshWallet()
        }, ::onError)
    }

    fun getAddress() {
        incrementTasks()
        model.getAddress().siaSubscribe({
            decrementTasks()
            address.value = it
            address.value = null
        }, ::onError)
    }

    fun getAddresses() {
        incrementTasks()
        model.getAddresses().siaSubscribe({
            decrementTasks()
            addresses.value = it
            addresses.value = null
        }, ::onError)
    }

    fun getSeeds() {
        incrementTasks()
        model.getSeeds("english").siaSubscribe({
            decrementTasks()
            seeds.value = it
            seeds.value = null
        }, ::onError)
    }

    fun create(password: String, force: Boolean, seed: String? = null) {
        incrementTasks()
        if (seed == null) {
            model.init(password, "english", force).siaSubscribe({ it ->
                setSuccess("Created wallet")
                refreshWallet()
                this.seed.value = it.primaryseed
            }, ::onError)
        } else {
            model.initSeed(password, "english", seed, force).siaSubscribe({
                setSuccess("Created wallet")
                refreshWallet()
                this.seed.value = seed
            }, ::onError)
        }
    }

    fun send(amount: String, destination: String) {
        incrementTasks()
        model.send(amount, destination)
                .siaSubscribe({
                    setSuccess("Sent ${amount.toSC()} SC to $destination")
                    refreshWallet()
                }, ::onError)
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        incrementTasks()
        model.changePassword(currentPassword, newPassword).siaSubscribe({
            setSuccess("Changed password")
        }, ::onError)
    }

    fun sweep(seed: String) {
        incrementTasks()
        model.sweep("english", seed).siaSubscribe({
            setSuccess("Scanning blockchain, please wait...")
        }, ::onError)
    }
}