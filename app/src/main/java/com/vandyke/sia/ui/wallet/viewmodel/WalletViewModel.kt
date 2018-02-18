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
import com.vandyke.sia.util.Analytics
import com.vandyke.sia.util.rx.*
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
    val walletMonthHistory = MutableLiveData<List<WalletData>>()
    val usd = MutableLiveData<ScValueData>()
    val consensus = MutableLiveData<ConsensusData>()
    val transactions = MutableLiveData<List<TransactionData>>()
    val activeTasks = NonNullLiveData(0)
    val refreshing = NonNullLiveData(false)
    val numPeers = NonNullLiveData(0)
    val success = SingleLiveEvent<String>()
    val error = SingleLiveEvent<Throwable>()

    /* seed is used specifically for when a new wallet is created - not for when calling /wallet/seeds */
    val seed = SingleLiveEvent<String>()

    init {
        /* subscribe to flowables from the repositories. Note that since they're flowables,
         * we only need to subscribe this once */
        walletRepository.wallet()
                .io()
                .main()
                .subscribe(wallet::setValue, ::onError)

        walletRepository.walletMonthHistory()
                .io()
                .main()
                .subscribe(walletMonthHistory::setValue, ::onError)
      
        walletRepository.transactions()
                .io()
                .main()
                .subscribe(transactions::setValue, ::onError)

        consensusRepository.consensus()
                .io()
                .main()
                .subscribe(consensus::setValue, ::onError)

        scValueRepository.mostRecent()
                .io()
                .main()
                .subscribe(usd::setValue, ::onError)
    }

    private fun onSuccess(msg: String) {
        success.value = msg
    }

    private fun onError(t: Throwable) {
        error.value = t
    }

    fun refreshAll() {
        /* We tell the relevant repositories to update their data from the Sia node. This will
           trigger necessary updates elsewhere in the VM, as a result of subscribing to flowables from the database. */
        Completable.mergeArrayDelayError(
                walletRepository.updateAll(),
                consensusRepository.updateConsensus(),
                gatewayRepository.getGateway()
                        .doAfterSuccess { numPeers.postValue(it.peers.size) }
                        .doOnError { numPeers.postValue(0) }
                        .toCompletable())
                .io()
                .main()
                .track(activeTasks)
                .track(refreshing)
                .subscribe({}, ::onError)

        /* we don't include this in the refresh task because it's remote and less reliable and speedy. And also not as integral. */
        scValueRepository.updateScValue()
                .io()
                .main()
                .subscribe({}, ::onError)
    }

    fun refreshWallet() {
        walletRepository.updateAll()
                .io()
                .main()
                .track(activeTasks)
                .subscribe({}, ::onError)
    }

    fun unlock(password: String) {
        walletRepository.unlock(password)
                .io()
                .main()
                .track(activeTasks)
                .subscribe({
                    onSuccess("Unlocked")
                    refreshWallet()
                }, ::onError)
    }

    fun lock() {
        walletRepository.lock()
                .io()
                .main()
                .track(activeTasks)
                .subscribe({
                    onSuccess("Locked")
                    refreshWallet()
                }, ::onError)
    }

    fun create(password: String, force: Boolean, seed: String? = null) {
        if (seed == null) {
            walletRepository.init(password, "english", force)
                    .io()
                    .main()
                    .track(activeTasks)
                    .subscribe({
                        onSuccess("Created wallet")
                        refreshWallet()
                        this.seed.value = it.primaryseed
                        Analytics.createWallet(false)
                    }, ::onError)
        } else {
            walletRepository.initSeed(password, "english", seed, force)
                    .io()
                    .main()
                    .track(activeTasks)
                    .subscribe({
                        onSuccess("Created wallet")
                        refreshWallet()
                        this.seed.value = seed
                        Analytics.createWallet(true)
                    }, ::onError)
        }
    }

    fun send(amount: String, destination: String) {
        walletRepository.send(amount, destination)
                .io()
                .main()
                .track(activeTasks)
                .subscribe({
                    onSuccess("Sent ${amount.toSC()} SC to $destination")
                    refreshWallet()
                    Analytics.sendSiacoin(amount.toSC())
                }, ::onError)
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        walletRepository.changePassword(currentPassword, newPassword)
                .io()
                .main()
                .track(activeTasks)
                .subscribe({
                    onSuccess("Changed password")
                    refreshWallet()
                }, ::onError)
    }

    fun sweep(seed: String) {
        walletRepository.sweep("english", seed)
                .io()
                .main()
                .track(activeTasks)
                .subscribe({
                    refreshWallet()
                    Analytics.sweepSeed()
                }, ::onError)
    }

    /* the below are exposed as Singles because it's more straightforward
     * for the child fragments that use these to just subscribe to them,
     * as opposed to them observing some LiveData and then calling a function
     * that will populate that LiveData */
    fun getAddress(): Single<AddressData> {
        return walletRepository.getAddress()
                .io()
                .main()
                .track(activeTasks)
                .doOnError(::onError)
                .doAfterSuccess { Analytics.viewAddress() }
    }

    fun getAddresses(): Single<List<AddressData>> {
        return walletRepository.getAddresses()
                .io()
                .main()
                .track(activeTasks)
                .doOnError(::onError)
    }

    fun getSeeds(): Single<SeedsData> {
        return walletRepository.getSeeds()
                .io()
                .main()
                .track(activeTasks)
                .doOnError(::onError)
    }
}