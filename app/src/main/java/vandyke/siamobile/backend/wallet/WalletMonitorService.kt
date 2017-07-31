/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.wallet

import com.android.volley.Response
import com.android.volley.VolleyError
import org.json.JSONException
import org.json.JSONObject
import vandyke.siamobile.api.WalletApiJava
import vandyke.siamobile.api.models.ConsensusModel
import vandyke.siamobile.api.models.TransactionsModel
import vandyke.siamobile.api.models.WalletModel
import vandyke.siamobile.api.networking.Consensus
import vandyke.siamobile.api.networking.SiaCallback
import vandyke.siamobile.api.networking.SiaError
import vandyke.siamobile.api.networking.Wallet
import vandyke.siamobile.backend.BaseMonitorService
import vandyke.siamobile.util.toSC
import java.math.BigDecimal

class WalletMonitorService : BaseMonitorService() {

    var balanceHastings: BigDecimal = BigDecimal("0")
        private set
    var balanceUsd: BigDecimal = BigDecimal("0")
        private set

    private var listeners: ArrayList<WalletUpdateListener> = ArrayList()

    override fun onCreate() {
        instance = this
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun refresh() {
        refreshBalanceAndStatus()
        refreshTransactions()
        refreshConsensus()
    }

    fun refreshBalanceAndStatus() {
        Wallet.wallet(SiaCallback({ sendBalanceUpdate(it) }, { sendBalanceError(it) }))

        WalletApiJava.coincapSC(object : Response.Listener<String> {
            override fun onResponse(response: String?) {
                try {
                    val json = JSONObject(response)
                    val usdPrice = json.getDouble("usdPrice")
                    balanceUsd = WalletApiJava.scToUsd(usdPrice, balanceHastings.toSC())
                } catch (e: JSONException) {
                    balanceUsd = BigDecimal("0")
                }
                sendUsdUpdate()
            }
        }, Response.ErrorListener { error -> sendUsdError(error) })
    }

    fun refreshTransactions() {
        Wallet.transactions(SiaCallback({ sendTransactionsUpdate(it) }, { sendTransactionsError(it) }))
//        WalletApiJava.transactions(object : SiaRequest.VolleyCallback {
//            override fun onSuccess(response: JSONObject) {
//                val mostRecentTxId = prefs.mostRecentTxId // TODO: can give false positives when switching between wallets
//                var newTxs = 0
//                var foundMostRecent = false
//                var netOfNewTxs = BigDecimal("0")
//                transactions.clear()
//                for (tx in Transaction.populateTransactions(response)) {
//                    if (tx.transactionId == mostRecentTxId)
//                        foundMostRecent = true
//                    transactions.add(tx)
//                    if (!foundMostRecent) {
//                        newTxs++
//                        netOfNewTxs = netOfNewTxs.add(tx.netValue)
//                    }
//                }
//                if (newTxs > 0) {
//                    prefs.mostRecentTxId = transactions[0].transactionId
//                    NotificationUtil.notification(this@WalletMonitorService, TRANSACTION_NOTIFICATION,
//                            R.drawable.ic_account_balance_white_48dp, newTxs.toString() + " new transaction" + if (newTxs > 1) "s" else "",
//                            "Net value: " + (if (netOfNewTxs > BigDecimal.ZERO) "+" else "") + WalletApiJava.round(SCUtil.hastingsToSc(netOfNewTxs)) + " SC",
//                            false)
//                }
//                sendTransactionsUpdate()
//            }
//
//            override fun onError(error: SiaRequest.Error) {
//                sendTransactionsError(error)
//            }
//        })
    }

    fun refreshConsensus() {
        Consensus.consensus(SiaCallback({ sendSyncUpdate(it) }, { sendSyncError(it) }))
    }

    interface WalletUpdateListener {
        fun onBalanceUpdate(walletModel: WalletModel)
        fun onUsdUpdate(service: WalletMonitorService)
        fun onTransactionsUpdate(transactionsModel: TransactionsModel)
        fun onSyncUpdate(consensusModel: ConsensusModel)
        fun onBalanceError(error: SiaError)
        fun onUsdError(error: VolleyError)
        fun onTransactionsError(error: SiaError)
        fun onSyncError(error: SiaError)
    }

    fun registerListener(listener: WalletUpdateListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: WalletUpdateListener) {
        listeners.remove(listener)
    }

    fun sendBalanceUpdate(walletModel: WalletModel) {
        for (listener in listeners)
            listener.onBalanceUpdate(walletModel)
    }

    fun sendUsdUpdate() {
        for (listener in listeners)
            listener.onUsdUpdate(this)
    }

    fun sendTransactionsUpdate(transactionsModel: TransactionsModel) {
        for (listener in listeners)
            listener.onTransactionsUpdate(transactionsModel)
    }

    fun sendSyncUpdate(consensusModel: ConsensusModel) {
        for (listener in listeners)
            listener.onSyncUpdate(consensusModel)
    }

    fun sendBalanceError(error: SiaError) {
        for (listener in listeners)
            listener.onBalanceError(error)
    }

    fun sendUsdError(error: VolleyError) {
        for (listener in listeners)
            listener.onUsdError(error)
    }

    fun sendTransactionsError(error: SiaError) {
        for (listener in listeners)
            listener.onTransactionsError(error)
    }

    fun sendSyncError(error: SiaError) {
        for (listener in listeners)
            listener.onSyncError(error)
    }

    companion object {
        private var instance: WalletMonitorService? = null

        fun staticRefresh() {
            instance?.refresh()
        }

        fun staticPostRunnable() {
            instance?.postRefreshRunnable()
        }
    }
}
