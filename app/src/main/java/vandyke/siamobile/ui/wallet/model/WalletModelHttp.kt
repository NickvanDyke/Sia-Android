/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.model

import vandyke.siamobile.backend.data.consensus.ConsensusData
import vandyke.siamobile.backend.data.wallet.*
import vandyke.siamobile.backend.networking.Consensus
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.Wallet

class WalletModelHttp : IWalletModel {

    override fun getWallet(callback: SiaCallback<WalletData>) = Wallet.wallet(callback)

    override fun getAddress(callback: SiaCallback<AddressData>) = Wallet.address(callback)

    override fun getAddresses(callback: SiaCallback<AddressesData>) = Wallet.addresses(callback)

    override fun getSeeds(dictionary: String, callback: SiaCallback<SeedsData>) = Wallet.seeds(dictionary, callback)

    override fun getTransactions(callback: SiaCallback<TransactionsData>) = Wallet.transactions(callback)

    override fun getConsensus(callback: SiaCallback<ConsensusData>) = Consensus.consensus(callback)

    override fun unlock(password: String, callback: SiaCallback<Unit>) = Wallet.unlock(password, callback)

    override fun lock(callback: SiaCallback<Unit>) = Wallet.lock(callback)

    override fun init(password: String, dictionary: String, force: Boolean, callback: SiaCallback<WalletInitData>)
        = Wallet.init(password, dictionary, force, callback)

    override fun initSeed(password: String, dictionary: String, seed: String, force: Boolean, callback: SiaCallback<Unit>)
        = Wallet.initSeed(password, dictionary, seed, force, callback)

    override fun send(amount: String, destination: String, callback: SiaCallback<Unit>) = Wallet.send(amount, destination, callback)

    override fun changePassword(currentPassword: String, newPassword: String, callback: SiaCallback<Unit>)
        = Wallet.changePassword(currentPassword, newPassword, callback)

    override fun sweep(dictionary: String, seed: String, callback: SiaCallback<Unit>) = Wallet.sweep(dictionary, seed, callback)
}