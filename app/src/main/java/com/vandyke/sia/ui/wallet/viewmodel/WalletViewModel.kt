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
import com.vandyke.sia.util.*
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
    val success = SingleLiveEvent<String>()
    val error = SingleLiveEvent<Throwable>()

    /* seed is used specifically for when a new wallet is created - not for when called /wallet/seeds */
    val seed = SingleLiveEvent<String>()

    init {
        /* subscribe to flowables from the repositories. Note that since they're flowables,
         * we only need to subscribe this once */
        walletRepository.wallet().io().main().subscribe({
            wallet.value = it
        }, ::onError)

        walletRepository.transactions().io().main().subscribe({
            transactions.value = it
        }, ::onError)

        consensusRepository.consensus().io().main().subscribe({
            consensus.value = it
        }, ::onError)

        scValueRepository.scValue().io().main().subscribe({
            usd.value = it
        }, ::onError)
    }

    /* success and error are immediately set back to null because the view only reacts on
       non-null values of them, and if they're holding a non-null value and the
       view is recreated, then it'll display the success/error even though it shouldn't.
       There might be a better way around that */
    private fun onSuccess(msg: String) {
        activeTasks.decrementZeroMin()
        success.value = msg
    }

    private fun onError(t: Throwable) {
        activeTasks.decrementZeroMin()
        error.value = t
    }

    fun refreshAll() {
        /* We tell the relevant repositories to update their data from the Sia node. This will
           trigger necessary updates elsewhere in the VM, as a result of subscribing to flowables from the database. */
        activeTasks.increment()
        refreshing.value = true
        Completable.mergeArrayDelayError(
                walletRepository.updateAll(),
                consensusRepository.updateConsensus(),
                gatewayRepository.getGateway().doAfterSuccess {
                    numPeers.postValue(it.peers.size)
                }.doOnError {
                    numPeers.postValue(0)
                }.toCompletable()
        ).io().main().subscribe({
            refreshing.value = false
            activeTasks.decrementZeroMin()
        }, {
            refreshing.value = false
            onError(it)
        })

        /* we don't include this in the refresh task because it's remote and less reliable and speedy. And also not as integral. */
        scValueRepository.updateScValue().io().main().subscribe({}, ::onError)
    }

    fun refreshWallet() {
        activeTasks.increment()
        walletRepository.updateAll().io().main().subscribe({ activeTasks.decrementZeroMin() }, ::onError)
    }

    fun unlock(password: String) {
        activeTasks.increment()
        walletRepository.unlock(password).io().main().subscribe({
            onSuccess("Unlocked")
            refreshWallet()
        }, ::onError)
    }

    fun lock() {
        activeTasks.increment()
        walletRepository.lock().io().main().subscribe({
            onSuccess("Locked")
            refreshWallet()
        }, ::onError)
    }

    fun create(password: String, force: Boolean, seed: String? = null) {
        activeTasks.increment()
        if (seed == null) {
            walletRepository.init(password, "english", force).io().main().subscribe({
                onSuccess("Created wallet")
                refreshWallet()
                this.seed.value = it.primaryseed
            }, ::onError)
        } else {
            walletRepository.initSeed(password, "english", seed, force).io().main().subscribe({
                onSuccess("Created wallet")
                refreshWallet()
                this.seed.value = seed
            }, ::onError)
        }
    }

    fun send(amount: String, destination: String) {
        activeTasks.increment()
        walletRepository.send(amount, destination).io().main().subscribe({
            onSuccess("Sent ${amount.toSC()} SC to $destination")
            refreshWallet()
        }, ::onError)
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        activeTasks.increment()
        walletRepository.changePassword(currentPassword, newPassword).io().main().subscribe({
            onSuccess("Changed password")
            refreshWallet()
        }, ::onError)
    }

    fun sweep(seed: String) {
        activeTasks.increment()
        walletRepository.sweep("english", seed).io().main().subscribe({
            activeTasks.decrementZeroMin()
            refreshWallet()
        }, ::onError)
    }

    /* the below are exposed as Singles because it's more straightforward
     * for the child fragments that use these to just subscribe to them,
     * as opposed to them observing some LiveData and then calling a function
     * that will populate that LiveData */
    fun getAddress(): Single<AddressData> {
        activeTasks.increment()
        return walletRepository.getAddress().io().main()
                .doOnError { onError(it) }
                .doAfterSuccess { activeTasks.decrementZeroMin() }
    }

    fun getAddresses(): Single<List<AddressData>> {
        activeTasks.increment()
        return walletRepository.getAddresses().io().main()
                .doOnError { onError(it) }
                .doAfterSuccess { activeTasks.decrementZeroMin() }
    }

    fun getSeeds(): Single<SeedsData> {
        activeTasks.increment()
        return walletRepository.getSeeds().io().main()
                .doOnError { onError(it) }
                .doAfterSuccess { activeTasks.decrementZeroMin() }
    }
}