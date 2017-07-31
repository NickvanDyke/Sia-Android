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
import vandyke.siamobile.R
import vandyke.siamobile.api.*
import vandyke.siamobile.backend.BaseMonitorService
import vandyke.siamobile.backend.wallet.transaction.Transaction
import vandyke.siamobile.misc.Utils
import vandyke.siamobile.prefs
import java.math.BigDecimal

class WalletMonitorService : BaseMonitorService() {

    enum class WalletStatus {
        NONE, LOCKED, UNLOCKED
    }

    lateinit var walletModel: WalletModel

     var walletStatus: WalletStatus = WalletStatus.NONE
        private set
     var balanceHastings: BigDecimal = BigDecimal("0")
        private set
     var balanceHastingsUnconfirmed: BigDecimal = BigDecimal("0")
        private set
     var balanceUsd: BigDecimal = BigDecimal("0")
        private set
     var transactions: ArrayList<Transaction> = ArrayList()
        private set
    var syncProgress: Double = 0.0
        private set
    var blockHeight: Long = 0
        private set

    private var listeners: ArrayList<WalletUpdateListener> = ArrayList()

    private val SYNC_NOTIFICATION = 0
    private val TRANSACTION_NOTIFICATION = 1

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
        refreshSyncProgress()
    }

    fun refreshBalanceAndStatus() {
        Wallet.wallet(callback({ response -> println("response") }, { t -> println("error") }))
        WalletApiJava.wallet(object : SiaRequest.VolleyCallback {
            override fun onSuccess(response: JSONObject) {
                if (response.getString("encrypted") == "false")
                    walletStatus = WalletStatus.NONE
                else if (response.getString("unlocked") == "false")
                    walletStatus = WalletStatus.LOCKED
                else
                    walletStatus = WalletStatus.UNLOCKED
                balanceHastings = BigDecimal(response.getString("confirmedsiacoinbalance"))
                balanceHastingsUnconfirmed = BigDecimal(response.getString("unconfirmedincomingsiacoins"))
                        .subtract(BigDecimal(response.getString("unconfirmedoutgoingsiacoins")))

                sendBalanceUpdate()
            }

            override fun onError(error: SiaRequest.Error) {
                sendBalanceError(error)
            }
        })

        WalletApiJava.coincapSC(object : Response.Listener<String> {
            override fun onResponse(response: String?) {
                try {
                    val json = JSONObject(response)
                    val usdPrice = json.getDouble("usdPrice")
                    balanceUsd = WalletApiJava.scToUsd(usdPrice, WalletApiJava.hastingsToSC(balanceHastings))
                } catch (e: JSONException) {
                    balanceUsd = BigDecimal("0")
                }
                sendUsdUpdate()
            }
        }, Response.ErrorListener { error -> sendUsdError(error) })
    }

    fun refreshTransactions() {
        WalletApiJava.transactions(object : SiaRequest.VolleyCallback {
            override fun onSuccess(response: JSONObject) {
                val mostRecentTxId = prefs.mostRecentTxId // TODO: can give false positives when switching between wallets
                var newTxs = 0
                var foundMostRecent = false
                var netOfNewTxs = BigDecimal("0")
                transactions.clear()
                for (tx in Transaction.populateTransactions(response)) {
                    if (tx.transactionId == mostRecentTxId)
                        foundMostRecent = true
                    transactions.add(tx)
                    if (!foundMostRecent) {
                        newTxs++
                        netOfNewTxs = netOfNewTxs.add(tx.netValue)
                    }
                }
                if (newTxs > 0) {
                    prefs.mostRecentTxId = transactions[0].transactionId
                    Utils.notification(this@WalletMonitorService, TRANSACTION_NOTIFICATION,
                            R.drawable.ic_account_balance_white_48dp, newTxs.toString() + " new transaction" + if (newTxs > 1) "s" else "",
                            "Net value: " + (if (netOfNewTxs > BigDecimal.ZERO) "+" else "") + WalletApiJava.round(WalletApiJava.hastingsToSC(netOfNewTxs)) + " SC",
                            false)
                }
                sendTransactionsUpdate()
            }

            override fun onError(error: SiaRequest.Error) {
                sendTransactionsError(error)
            }
        })
    }

    fun refreshSyncProgress() {
        Consensus.consensus(object : SiaRequest.VolleyCallback {
            override fun onSuccess(response: JSONObject) {
                blockHeight = response.getLong("height")
                if (response.getBoolean("synced")) {
                    syncProgress = 100.0
                    Utils.cancelNotification(this@WalletMonitorService, SYNC_NOTIFICATION) // TODO: maybe have separate service for notifications that registers a listener... not sure if worth it
                } else {
                    syncProgress = response.getInt("height").toDouble() / estimatedBlockHeightAt(System.currentTimeMillis() / 1000) * 100
                    if (syncProgress == 0.0)
                        Utils.cancelNotification(this@WalletMonitorService, SYNC_NOTIFICATION)
                    else
                        Utils.notification(this@WalletMonitorService, SYNC_NOTIFICATION, R.drawable.ic_sync_white_48dp,
                                "Syncing...", String.format("Progress (estimated): %.2f%%", syncProgress), false)
                }

                sendSyncUpdate()
            }

            override fun onError(error: SiaRequest.Error) {
                sendSyncError(error)
                Utils.notification(this@WalletMonitorService, SYNC_NOTIFICATION, R.drawable.ic_sync_problem_white_48dp,
                        "Syncing...", "Error retrieving sync progress", false)
            }
        })
    }

    interface WalletUpdateListener {
        fun onBalanceUpdate(walletModel: WalletModel)

        fun onUsdUpdate(service: WalletMonitorService)

        fun onTransactionsUpdate(service: WalletMonitorService)

        fun onSyncUpdate(service: WalletMonitorService)

        fun onBalanceError(error: SiaRequest.Error)

        fun onUsdError(error: VolleyError)

        fun onTransactionsError(error: SiaRequest.Error)

        fun onSyncError(error: SiaRequest.Error)
    }

    fun registerListener(listener: WalletUpdateListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: WalletUpdateListener) {
        listeners.remove(listener)
    }

    fun sendBalanceUpdate() {
        for (listener in listeners)
            listener.onBalanceUpdate(walletModel)
    }

    fun sendUsdUpdate() {
        for (listener in listeners)
            listener.onUsdUpdate(this)
    }

    fun sendTransactionsUpdate() {
        for (listener in listeners)
            listener.onTransactionsUpdate(this)
    }

    fun sendSyncUpdate() {
        for (listener in listeners)
            listener.onSyncUpdate(this)
    }

    fun sendBalanceError(error: SiaRequest.Error) {
        for (listener in listeners)
            listener.onBalanceError(error)
    }

    fun sendUsdError(error: VolleyError) {
        for (listener in listeners)
            listener.onUsdError(error)
    }

    fun sendTransactionsError(error: SiaRequest.Error) {
        for (listener in listeners)
            listener.onTransactionsError(error)
    }

    fun sendSyncError(error: SiaRequest.Error) {
        for (listener in listeners)
            listener.onSyncError(error)
    }

    fun estimatedBlockHeightAt(time: Long): Int {
        val block100kTimestamp: Long = 1492126789 // Unix timestamp; seconds
        val blockTime = 9 // overestimate
        val diff = time - block100kTimestamp
        return (100000 + diff / 60 / blockTime.toLong()).toInt()
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
