/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.model

import vandyke.siamobile.backend.data.consensus.ConsensusData
import vandyke.siamobile.backend.data.wallet.*
import vandyke.siamobile.backend.networking.SiaCallback

interface IWalletModel {
    fun getWallet(callback: SiaCallback<WalletData>)
    fun getAddress(callback: SiaCallback<AddressData>)
    fun getAddresses(callback: SiaCallback<AddressesData>)
    fun getTransactions(callback: SiaCallback<TransactionsData>)
    fun getSeeds(dictionary: String, callback: SiaCallback<SeedsData>)
    fun getConsensus(callback: SiaCallback<ConsensusData>)
    fun unlock(password: String, callback: SiaCallback<Unit>)
    fun lock(callback: SiaCallback<Unit>)
    fun init(password: String, dictionary: String, force: Boolean, callback: SiaCallback<WalletInitData>)
    fun initSeed(password: String, dictionary: String, seed: String, force: Boolean, callback: SiaCallback<Unit>)
    fun send(amount: String, destination: String, callback: SiaCallback<Unit>)
    fun changePassword(currentPassword: String, newPassword: String, callback: SiaCallback<Unit>)
    fun sweep(dictionary: String, seed: String, callback: SiaCallback<Unit>)
}