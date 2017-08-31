/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet

import vandyke.siamobile.backend.networking.Consensus
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.Wallet

class WalletPresenter(private val view: IWalletView) : IWalletPresenter {

    override fun refresh() {
        refreshBalanceAndStatus()
        refreshTransactions()
        refreshConsensus()
    }

    fun refreshBalanceAndStatus() {
        Wallet.wallet(SiaCallback({ it -> view.onWalletUpdate(it) }, { view.onWalletError(it) }))
        Wallet.scPrice(SiaCallback({ it -> view.onUsdUpdate(it) }, { view.onUsdError(it) }))
    }

    fun refreshTransactions() {
        Wallet.transactions(SiaCallback({ it ->
            view.onTransactionsUpdate(it)
//            val mostRecentTxId = prefs.mostRecentTxId // TODO: can give false positives when switching between wallets
//            var newTxs = 0
//            var netOfNewTxs = BigDecimal.ZERO
//            for (tx in it.alltransactions) {
//                if (tx.transactionid == mostRecentTxId) {
//                    break
//                } else {
//                    newTxs++
//                    netOfNewTxs = netOfNewTxs.add(tx.netValue)
//                }
//            }
//            if (newTxs > 0) {
//                prefs.mostRecentTxId = it.alltransactions[0].transactionid
//                NotificationUtil.notification(this@WalletService, TRANSACTION_NOTIFICATION,
//                        R.drawable.ic_new_transactions, newTxs.toString() + " new transaction" + if (newTxs > 1) "s" else "",
//                        "Net value: " + (if (netOfNewTxs > BigDecimal.ZERO) "+" else "") + netOfNewTxs.toSC().round().toPlainString() + " SC",
//                        false)
//            }
        }, {
            view.onTransactionsError(it)
        }))
    }

    fun refreshConsensus() {
        Consensus.consensus(SiaCallback({ it ->
            view.onConsensusUpdate(it)
//            if (it.syncprogress == 0.0 || it.synced) {
//                NotificationUtil.cancelNotification(this@WalletService, SYNC_NOTIFICATION)
//            } else {
//                NotificationUtil.notification(this@WalletService, SYNC_NOTIFICATION, R.drawable.ic_sync,
//                        "Syncing...", String.format("Progress (estimated): %.2f%%", it.syncprogress), false)
//            }
        }, {
            view.onConsensusError(it)
        }))
    }

}