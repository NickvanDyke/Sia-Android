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
import com.vandyke.sia.db
import com.vandyke.sia.siadOutput
import com.vandyke.sia.util.NonNullLiveData
import com.vandyke.sia.util.siaSubscribe
import com.vandyke.sia.util.toSC
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class WalletViewModel : ViewModel() {
    val wallet = MutableLiveData<WalletData>()
    val usd = MutableLiveData<ScValueData>()
    val consensus = MutableLiveData<ConsensusData>()
    val transactions = MutableLiveData<List<TransactionData>>()
    val activeTasks = NonNullLiveData(0) // TODO: ideally, also represent refreshing, separately from the number of active tasks
    val refreshing = NonNullLiveData(false)
    val numPeers = NonNullLiveData(0)
    val success = MutableLiveData<String>()
    val error = MutableLiveData<SiaError>()

    /* seed is used specifically for when a new wallet is created - not for when called /wallet/seeds */
    val seed = MutableLiveData<String>()

    /* when you see a LiveData's value being set to null immediately after, it's so that an observer
       won't receive anything upon initial subscription to it (normally it still would, but generally
       an extension function is used that doesn't pass the value unless it's not null) */

    // TODO: inject these
    private val walletRepo = WalletRepository(siaApi, db)
    private val consensusRepo = ConsensusRepository()
    private val scValueRepo = ScValueRepository()

    private val subscription = siadOutput.observeOn(AndroidSchedulers.mainThread()).subscribe {
        if (it.contains("Finished loading") || it.contains("Done!"))
            refreshAll()
    }

    init {
        /* subscribe to flowables from the repositories. Note that since they're flowables,
         * we only need to subscribe this once */
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
        activeTasks.value = activeTasks.value + 1
    }

    private fun decrementTasks() {
        if (activeTasks.value > 0)
            activeTasks.value = activeTasks.value - 1
    }

    fun refreshAll() {
        /* We tell the relevant repositories to update their data from the Sia node. This will
           trigger necessary updates elsewhere in the VM, as a result of subscribing to flowables from the database. */
        incrementTasks()
        refreshing.value = true
        Completable.mergeArray(
                walletRepo.updateAll(),
                consensusRepo.updateConsensus(),
                scValueRepo.updateScValue(),
                siaApi.gateway().doAfterSuccess {
                    numPeers.postValue(it.peers.size)
                }.doOnError {
                    numPeers.postValue(0)
                }.toCompletable()
        ).siaSubscribe({
            refreshing.value = false
            decrementTasks()
        }, {
            refreshing.value = false
            decrementTasks()
        })
    }

    fun refreshWallet() {
        incrementTasks()
        walletRepo.updateAll().siaSubscribe(::decrementTasks, ::onError)
    }

    fun unlock(password: String) {
        incrementTasks()
        walletRepo.unlock(password).siaSubscribe({
            setSuccess("Unlocked")
            refreshWallet()
        }, ::onError)
    }

    fun lock() {
        incrementTasks()
        walletRepo.lock().siaSubscribe({
            setSuccess("Locked")
            refreshWallet()
        }, ::onError)
    }

    fun create(password: String, force: Boolean, seed: String? = null) {
        incrementTasks()
        if (seed == null) {
            walletRepo.init(password, "english", force).siaSubscribe({ it ->
                setSuccess("Created wallet")
                refreshWallet()
                this.seed.value = it.primaryseed
                this.seed.value = null
            }, ::onError)
        } else {
            walletRepo.initSeed(password, "english", seed, force).siaSubscribe({
                setSuccess("Created wallet")
                refreshWallet()
                this.seed.value = seed
                this.seed.value = null
            }, ::onError)
        }
    }

    fun send(amount: String, destination: String) {
        incrementTasks()
        walletRepo.send(amount, destination).siaSubscribe({
            setSuccess("Sent ${amount.toSC()} SC to $destination")
            refreshWallet()
        }, ::onError)
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        incrementTasks()
        walletRepo.changePassword(currentPassword, newPassword).siaSubscribe({
            setSuccess("Changed password")
            refreshWallet()
        }, ::onError)
    }

    fun sweep(seed: String) {
        incrementTasks()
        walletRepo.sweep("english", seed).siaSubscribe({
            decrementTasks()
            refreshWallet()
        }, ::onError)
    }

    /* the below are exposed as Singles because it's more straightforward
     * for the child fragments that use these to just subscribe to them,
     * as opposed to them observing some LiveData and then calling a function
     * that will populate that LiveData */
    fun getAddress(): Single<AddressData> {
        incrementTasks()
        return walletRepo.getAddress()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { onError(it as SiaError) }
                .doAfterSuccess { decrementTasks() }
    }

    fun getAddresses(): Single<List<AddressData>> {
        incrementTasks()
        return walletRepo.getAddresses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { onError(it as SiaError) }
                .doAfterSuccess { decrementTasks() }
    }

    fun getSeeds(): Single<SeedsData> {
        incrementTasks()
        return walletRepo.getSeeds()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { onError(SiaError(it)) }
                .doAfterSuccess { decrementTasks() }
    }
}