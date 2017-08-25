/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.wallet

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import vandyke.siamobile.R
import vandyke.siamobile.backend.BaseMonitorService
import vandyke.siamobile.backend.models.consensus.ConsensusModel
import vandyke.siamobile.backend.networking.Consensus
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.backend.networking.Wallet
import vandyke.siamobile.backend.siad.Siad
import vandyke.siamobile.backend.wallet.models.ScPriceModel
import vandyke.siamobile.backend.wallet.models.TransactionsModel
import vandyke.siamobile.backend.wallet.models.WalletModel
import vandyke.siamobile.prefs
import vandyke.siamobile.util.NotificationUtil
import vandyke.siamobile.util.round
import vandyke.siamobile.util.toSC
import java.math.BigDecimal

class WalletService : BaseMonitorService(), Siad.SiadListener {

    private val TRANSACTION_NOTIFICATION: Int = 3
    private val SYNC_NOTIFICATION: Int = 2

    private var listeners = ArrayList<WalletUpdateListener>()

    override fun onCreate() {
        super.onCreate()
        Siad.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Siad.removeListener(this)
    }

    override fun refresh() {
        refreshBalanceAndStatus()
        refreshTransactions()
        refreshConsensus()
    }

    fun refreshBalanceAndStatus() {
        Wallet.wallet(SiaCallback({ it -> sendBalanceUpdate(it) }, { sendBalanceError(it) }))
        Wallet.scPrice(SiaCallback({ it -> sendUsdUpdate(it) }, { sendUsdError(it) }))
    }

    fun refreshTransactions() {
        Wallet.transactions(SiaCallback({ it ->
            sendTransactionsUpdate(it)
            val mostRecentTxId = prefs.mostRecentTxId // TODO: can give false positives when switching between wallets
            var newTxs = 0
            var netOfNewTxs = BigDecimal.ZERO
            for (tx in it.alltransactions) {
                if (tx.transactionid == mostRecentTxId) {
                    break
                } else {
                    newTxs++
                    netOfNewTxs = netOfNewTxs.add(tx.netValue)
                }
            }
            if (newTxs > 0) {
                prefs.mostRecentTxId = it.alltransactions[0].transactionid
                NotificationUtil.notification(this@WalletService, TRANSACTION_NOTIFICATION,
                        R.drawable.ic_new_transactions, newTxs.toString() + " new transaction" + if (newTxs > 1) "s" else "",
                        "Net value: " + (if (netOfNewTxs > BigDecimal.ZERO) "+" else "") + netOfNewTxs.toSC().round().toPlainString() + " SC",
                        false)
            }
        }, { sendTransactionsError(it) }))
    }

    fun refreshConsensus() {
        Consensus.consensus(SiaCallback({ it ->
            sendSyncUpdate(it)
            if (it.syncprogress == 0.0 || it.synced) {
                NotificationUtil.cancelNotification(this@WalletService, SYNC_NOTIFICATION)
            } else {
                NotificationUtil.notification(this@WalletService, SYNC_NOTIFICATION, R.drawable.ic_sync,
                        "Syncing...", String.format("Progress (estimated): %.2f%%", it.syncprogress), false)
            }
        }, { sendSyncError(it) }))
    }

    override fun onSiadOutput(line: String) {
        if (line.contains("Finished loading") || line.contains("Done!"))
            refresh()
    }

    interface WalletUpdateListener {
        fun onBalanceUpdate(walletModel: WalletModel)
        fun onUsdUpdate(scPriceModel: ScPriceModel)
        fun onTransactionsUpdate(transactionsModel: TransactionsModel)
        fun onSyncUpdate(consensusModel: ConsensusModel)
        fun onBalanceError(error: SiaError)
        fun onUsdError(error: SiaError)
        fun onTransactionsError(error: SiaError)
        fun onSyncError(error: SiaError)
    }

    fun registerListener(listener: WalletUpdateListener) = listeners.add(listener)

    fun unregisterListener(listener: WalletUpdateListener) = listeners.remove(listener)

    fun sendBalanceUpdate(walletModel: WalletModel) = listeners.forEach { it.onBalanceUpdate(walletModel) }

    fun sendUsdUpdate(scPriceModel: ScPriceModel) = listeners.forEach { it.onUsdUpdate(scPriceModel) }

    fun sendTransactionsUpdate(transactionsModel: TransactionsModel) = listeners.forEach { it.onTransactionsUpdate(transactionsModel) }

    fun sendSyncUpdate(consensusModel: ConsensusModel) = listeners.forEach { it.onSyncUpdate(consensusModel) }

    fun sendBalanceError(error: SiaError) = listeners.forEach { it.onBalanceError(error) }

    fun sendUsdError(error: SiaError) = listeners.forEach { it.onUsdError(error) }

    fun sendTransactionsError(error: SiaError) = listeners.forEach { it.onTransactionsError(error) }

    fun sendSyncError(error: SiaError) = listeners.forEach { it.onSyncError(error) }

    companion object {
        fun singleAction(context: Context, action: (service: WalletService) -> Unit) {
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    action((service as LocalBinder).service as WalletService)
                    context.unbindService(this)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                }
            }
            context.bindService(Intent(context, WalletService::class.java), connection, Context.BIND_AUTO_CREATE)
        }
    }
}
