/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.presenter

import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.backend.networking.Wallet
import vandyke.siamobile.ui.wallet.model.IWalletModel
import vandyke.siamobile.ui.wallet.view.IWalletView

class WalletPresenter(private val walletView: IWalletView, val walletModel: IWalletModel) : IWalletPresenter {

    override fun refresh() {
        refreshWallet()
        refreshTransactions()
        refreshConsensus()
    }

    fun refreshWallet() {
        walletModel.getWallet(SiaCallback({ it -> walletView.onWalletUpdate(it) }, { walletView.onWalletError(it) }))
        Wallet.scPrice(SiaCallback({ it -> walletView.onUsdUpdate(it) }, { walletView.onUsdError(it) }))
    }

    fun refreshTransactions() {
        walletModel.getTransactions(SiaCallback({ it -> walletView.onTransactionsUpdate(it) }, { walletView.onTransactionsError(it) }))
    }

    fun refreshConsensus() {
        walletModel.getConsensus(SiaCallback({ it -> walletView.onConsensusUpdate(it) }, { walletView.onConsensusError(it) }))
    }

    override fun unlock(password: String) {
        walletModel.unlock(password, SiaCallback({ ->
            walletView.onSuccess()
            refreshWallet()
            walletView.closeExpandableFrame()
        }, {
            if (it.reason == SiaError.Reason.WALLET_SCAN_IN_PROGRESS)
                walletView.closeExpandableFrame()
            walletView.onError(it)
        }))
    }

    override fun lock() {
        walletModel.lock(SiaCallback({ ->
            walletView.onSuccess()
            refreshWallet()
        }, {
            walletView.onError(it)
        }))
    }

    override fun create(password: String, force: Boolean, seed: String?) {
        if (seed == null) {
            walletModel.init(password, "english", force, callback = SiaCallback({ it ->
                walletView.onSuccess()
                refreshWallet()
                walletView.closeExpandableFrame()
                walletView.onWalletCreated(it.primaryseed)
            }, {
                walletView.onError(it)
            }))
        } else {
            walletModel.initSeed(password, "english", seed, force, SiaCallback({ ->
                walletView.onSuccess()
                refreshWallet()
                walletView.closeExpandableFrame()
                walletView.onWalletCreated(seed)
            }, {
                walletView.onError(it)
            }))
        }
    }

    override fun send(amount: String, destination: String) {
        walletModel.send(amount, destination, SiaCallback({ ->
            walletView.onSuccess()
            refreshWallet()
            walletView.closeExpandableFrame()
        }, {
            walletView.onError(it)
        }))
    }

    override fun changePassword(currentPassword: String, newPassword: String) {
        walletModel.changePassword(currentPassword, newPassword, SiaCallback({ ->
            walletView.onSuccess()
            walletView.closeExpandableFrame()
        }, {
            walletView.onError(it)
        }))
    }

    override fun sweep(seed: String) {
        walletModel.sweep("english", seed, SiaCallback({ ->
            walletView.onError(SiaError(SiaError.Reason.WALLET_SCAN_IN_PROGRESS))
            walletView.onSuccess()
            walletView.closeExpandableFrame()
        }, {
            walletView.onError(it)
        }))
    }
}