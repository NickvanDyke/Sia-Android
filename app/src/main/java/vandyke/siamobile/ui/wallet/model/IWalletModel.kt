/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.model

import io.reactivex.Completable
import io.reactivex.Single
import vandyke.siamobile.backend.data.consensus.ConsensusData
import vandyke.siamobile.backend.data.wallet.*

interface IWalletModel {
    fun getWallet(): Single<WalletData>
    fun getAddress(): Single<AddressData>
    fun getAddresses(): Single<AddressesData>
    fun getTransactions(): Single<TransactionsData>
    fun getSeeds(dictionary: String): Single<SeedsData>
    fun getConsensus(): Single<ConsensusData>
    fun unlock(password: String): Completable
    fun lock(): Completable
    fun init(password: String, dictionary: String, force: Boolean): Single<WalletInitData>
    fun initSeed(password: String, dictionary: String, seed: String, force: Boolean): Completable
    fun send(amount: String, destination: String): Completable
    fun changePassword(currentPassword: String, newPassword: String): Completable
    fun sweep(dictionary: String, seed: String): Completable
}