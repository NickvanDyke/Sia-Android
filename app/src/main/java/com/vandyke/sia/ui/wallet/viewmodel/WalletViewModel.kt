/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.models.consensus.ConsensusData
import com.vandyke.sia.data.models.wallet.*
import com.vandyke.sia.data.repository.ConsensusRepository
import com.vandyke.sia.data.repository.GatewayRepository
import com.vandyke.sia.data.repository.ScValueRepository
import com.vandyke.sia.data.repository.WalletRepository
import com.vandyke.sia.util.NonNullLiveData
import com.vandyke.sia.util.io
import com.vandyke.sia.util.main
import com.vandyke.sia.util.toSC
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class WalletViewModel
@Inject constructor(
        private val walletRepository: WalletRepository,
        private val consensusRepository: ConsensusRepository,
        private val scValueRepository: ScValueRepository,
        private val gatewayRepository: GatewayRepository
) : ViewModel() {
    val wallet = MutableLiveData<WalletData>()
    val usd = MutableLiveData<ScValueData>()
    val consensus = MutableLiveData<ConsensusData>()
    val transactions = MutableLiveData<List<TransactionData>>()
    val activeTasks = NonNullLiveData(0)
    val refreshing = NonNullLiveData(false)
    val numPeers = NonNullLiveData(0)
    val success = MutableLiveData<String>()
    val error = MutableLiveData<Throwable>()

    /* seed is used specifically for when a new wallet is created - not for when called /wallet/seeds */
    val seed = MutableLiveData<String>()

    /* when you see a LiveData's value being set to null immediately after, it's so that an observer
       won't receive anything upon initial subscription to it (normally it still would, but generally
       an extension function is used that doesn't pass the value unless it's not null) */

    init {
        /* subscribe to flowables from the repositories. Note that since they're flowables,
         * we only need to subscribe this once */
        this.walletRepository.wallet().io().main().subscribe({
            wallet.value = it
        }, ::onError)

        this.walletRepository.transactions().io().main().subscribe({
            transactions.value = it
        }, ::onError)

        this.consensusRepository.consensus().io().main().subscribe({
            consensus.value = it
        }, ::onError)

        this.scValueRepository.scValue().io().main().subscribe({
            usd.value = it
        }, ::onError)
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

    private fun onError(t: Throwable) {
        decrementTasks()
        error.value = t
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
        Completable.mergeArrayDelayError(
                this.walletRepository.updateAll(),
                this.consensusRepository.updateConsensus(),
                this.scValueRepository.updateScValue(),
                this.gatewayRepository.getGateway().doAfterSuccess {
                    numPeers.postValue(it.peers.size)
                }.doOnError {
                    numPeers.postValue(0)
                }.toCompletable()
        ).io().main().subscribe({
            refreshing.value = false
            decrementTasks()
        }, {
            refreshing.value = false
            onError(it)
        })
    }

    fun refreshWallet() {
        incrementTasks()
        this.walletRepository.updateAll().io().main().subscribe(::decrementTasks, ::onError)
    }

    fun unlock(password: String) {
        incrementTasks()
        this.walletRepository.unlock(password).io().main().subscribe({
            setSuccess("Unlocked")
            refreshWallet()
        }, ::onError)
    }

    fun lock() {
        incrementTasks()
        this.walletRepository.lock().io().main().subscribe({
            setSuccess("Locked")
            refreshWallet()
        }, ::onError)
    }

    fun create(password: String, force: Boolean, seed: String? = null) {
        incrementTasks()
        if (seed == null) {
            this.walletRepository.init(password, "english", force).io().main().subscribe({ it ->
                setSuccess("Created wallet")
                refreshWallet()
                this.seed.value = it.primaryseed
                this.seed.value = null
            }, ::onError)
        } else {
            this.walletRepository.initSeed(password, "english", seed, force).io().main().subscribe({
                setSuccess("Created wallet")
                refreshWallet()
                this.seed.value = seed
                this.seed.value = null
            }, ::onError)
        }
    }

    fun send(amount: String, destination: String) {
        incrementTasks()
        this.walletRepository.send(amount, destination).io().main().subscribe({
            setSuccess("Sent ${amount.toSC()} SC to $destination")
            refreshWallet()
        }, ::onError)
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        incrementTasks()
        this.walletRepository.changePassword(currentPassword, newPassword).io().main().subscribe({
            setSuccess("Changed password")
            refreshWallet()
        }, ::onError)
    }

    fun sweep(seed: String) {
        incrementTasks()
        this.walletRepository.sweep("english", seed).io().main().subscribe({
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
        return this.walletRepository.getAddress().io().main()
                .doOnError { onError(it) }
                .doAfterSuccess { decrementTasks() }
    }

    fun getAddresses(): Single<List<AddressData>> {
        incrementTasks()
        return this.walletRepository.getAddresses().io().main()
                .doOnError { onError(it) }
                .doAfterSuccess { decrementTasks() }
    }

    fun getSeeds(): Single<SeedsData> {
        incrementTasks()
        return this.walletRepository.getSeeds().io().main()
                .doOnError { onError(it) }
                .doAfterSuccess { decrementTasks() }
    }
}