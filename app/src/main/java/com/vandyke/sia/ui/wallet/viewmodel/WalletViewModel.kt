/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.SiaError
import com.vandyke.sia.data.models.consensus.ConsensusData
import com.vandyke.sia.data.models.wallet.*
import com.vandyke.sia.data.remote.siaApi
import com.vandyke.sia.data.repository.ConsensusRepository
import com.vandyke.sia.data.repository.ScValueRepository
import com.vandyke.sia.data.repository.WalletRepository
import com.vandyke.sia.siadOutput
import com.vandyke.sia.util.siaSubscribe
import com.vandyke.sia.util.toSC
import io.reactivex.android.schedulers.AndroidSchedulers

class WalletViewModel : ViewModel() {
    val wallet = MutableLiveData<WalletData>()
    val usd = MutableLiveData<ScValueData>()
    val consensus = MutableLiveData<ConsensusData>()
    val transactions = MutableLiveData<List<TransactionData>>()
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
    val addresses = MutableLiveData<List<AddressData>>()
    val seeds = MutableLiveData<SeedsData>()

    // TODO: inject these
    private val walletRepo = WalletRepository()
    private val consensusRepo = ConsensusRepository()
    private val scValueRepo = ScValueRepository()

    private val subscription = siadOutput.observeOn(AndroidSchedulers.mainThread()).subscribe {
        if (it.contains("Finished loading") || it.contains("Done!"))
            refresh()
    }

    init {
        activeTasks.value = 0
        /* subscribe to flowables from the database. Note that since they're flowables, they'll update
           when their results update, and therefore we don't need to do anything but subscribe this once */
        walletRepo.wallet().siaSubscribe({
            wallet.value = it
        }, ::onError)

        walletRepo.transactions().siaSubscribe({
            transactions.value = it
        }, ::onError)

        consensusRepo.consensus().siaSubscribe({
            consensus.value = it
        }, ::onError)

        scValueRepo.scValue().siaSubscribe({
            usd.value = it
        }, ::onError)
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
        /* We tell the relevant repositories to update their data from the Sia node. This will
           trigger necessary updates elsewhere in the VM, as a result of subscribing to flowables from the database. */
        incrementTasks()
        walletRepo.updateAllWalletStuff().siaSubscribe(::decrementTasks, ::onError)

        incrementTasks()
        consensusRepo.updateConsensus().siaSubscribe(::decrementTasks, ::onError)

        scValueRepo.updateScValue().siaSubscribe({}, ::onError)

        refreshPeers()
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
        walletRepo.unlock(password).siaSubscribe({
            setSuccess("Unlocked") // TODO: maybe have cool animations eventually, to indicate locking/unlocking/creating?
        }, ::onError)
    }

    fun lock() {
        incrementTasks()
        walletRepo.lock().siaSubscribe({
            setSuccess("Locked")
        }, ::onError)
    }

    fun getAddress() {
        incrementTasks()
        walletRepo.getAddress().siaSubscribe({
            decrementTasks()
            address.value = it
            address.value = null
        }, ::onError)
    }

    fun getAddresses() {
        incrementTasks()
        walletRepo.addresses().siaSubscribe({
            decrementTasks()
            addresses.value = it
            addresses.value = null
        }, ::onError)
    }

    fun getSeeds() {
        incrementTasks()
        walletRepo.getSeeds().siaSubscribe({
            decrementTasks()
            seeds.value = it
            seeds.value = null
        }, ::onError)
    }

    fun create(password: String, force: Boolean, seed: String? = null) {
        incrementTasks()
        if (seed == null) {
            walletRepo.init(password, "english", force).siaSubscribe({ it ->
                setSuccess("Created wallet")
                refresh()
                this.seed.value = it.primaryseed
            }, ::onError)
        } else {
            walletRepo.initSeed(password, "english", seed, force).siaSubscribe({
                setSuccess("Created wallet")
                refresh()
                this.seed.value = seed
            }, ::onError)
        }
    }

    fun send(amount: String, destination: String) {
        incrementTasks()
        walletRepo.send(amount, destination)
                .siaSubscribe({
                    setSuccess("Sent ${amount.toSC()} SC to $destination")
                    refresh()
                }, ::onError)
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        incrementTasks()
        walletRepo.changePassword(currentPassword, newPassword).siaSubscribe({
            setSuccess("Changed password")
        }, ::onError)
    }

    fun sweep(seed: String) {
        incrementTasks()
        walletRepo.sweep("english", seed).siaSubscribe(::decrementTasks, ::onError)
    }
}