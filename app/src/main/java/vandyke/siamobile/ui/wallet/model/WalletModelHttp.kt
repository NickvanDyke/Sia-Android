/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.model

import vandyke.siamobile.backend.networking.siaApi

class WalletModelHttp : IWalletModel {

    override fun getWallet() = siaApi.wallet()

    override fun getAddress() = siaApi.walletAddress()

    override fun getAddresses() = siaApi.walletAddresses()

    override fun getSeeds(dictionary: String) = siaApi.walletSeeds(dictionary)

    override fun getTransactions() = siaApi.walletTransactions()

    override fun getConsensus() = siaApi.consensus()

    override fun unlock(password: String) = siaApi.walletUnlock(password)

    override fun lock() = siaApi.walletLock()

    override fun init(password: String, dictionary: String, force: Boolean)
            = siaApi.walletInit(password, dictionary, force)

    override fun initSeed(password: String, dictionary: String, seed: String, force: Boolean)
            = siaApi.walletInitSeed(password, dictionary, seed, force)

    override fun send(amount: String, destination: String) = siaApi.walletSiacoins(amount, destination)

    override fun changePassword(currentPassword: String, newPassword: String)
            = siaApi.walletChangePassword(currentPassword, newPassword)

    override fun sweep(dictionary: String, seed: String) = siaApi.walletSweepSeed(dictionary, seed)
}