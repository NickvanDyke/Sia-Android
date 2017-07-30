/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet

import android.app.Fragment
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.fragment_wallet.*
import org.json.JSONObject
import vandyke.siamobile.MainActivity
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.Wallet
import vandyke.siamobile.backend.BaseMonitorService
import vandyke.siamobile.backend.coldstorage.ColdStorageHttpServer
import vandyke.siamobile.backend.wallet.WalletMonitorService
import vandyke.siamobile.backend.wallet.transaction.Transaction
import vandyke.siamobile.misc.Utils
import vandyke.siamobile.prefs
import vandyke.siamobile.wallet.dialogs.*
import vandyke.siamobile.wallet.transactionslist.TransactionExpandableGroup
import vandyke.siamobile.wallet.transactionslist.TransactionListAdapter
import java.math.BigDecimal
import java.util.*

class WalletFragment : Fragment(), WalletMonitorService.WalletUpdateListener {

    private val transactionExpandableGroups = ArrayList<TransactionExpandableGroup>()

    private lateinit var connection: ServiceConnection
    private lateinit var walletMonitorService: WalletMonitorService
    private var bound = false

    private lateinit var refreshButton: MenuItem
    private lateinit var statusButton: MenuItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        if (MainActivity.appTheme === MainActivity.Theme.AMOLED || MainActivity.appTheme === MainActivity.Theme.CUSTOM) {
            top_shadow.visibility = View.GONE
        } else if (MainActivity.appTheme === MainActivity.Theme.DARK) {
            top_shadow.setBackgroundResource(R.drawable.top_shadow_dark)
        }
        if (MainActivity.appTheme === MainActivity.Theme.AMOLED) {
            receiveButton.setBackgroundColor(android.R.color.transparent)
            sendButton.setBackgroundColor(android.R.color.transparent)
        }

        val layoutManager = LinearLayoutManager(activity)
        transactionList.layoutManager = layoutManager
        transactionList.addItemDecoration(DividerItemDecoration(transactionList.context, layoutManager.orientation))

        sendButton.setOnClickListener { replaceExpandFrame(WalletSendDialog()) }
        receiveButton.setOnClickListener { replaceExpandFrame(WalletReceiveDialog()) }

        balanceText.setOnClickListener { v ->
            if (prefs.operationMode == "cold_storage") {
                ColdStorageHttpServer.showColdStorageHelp(v.context)
            } else {
                Utils.getDialogBuilder(v.context)
                        .setTitle("Exact Balance")
                        .setMessage("${Wallet.hastingsToSC(walletMonitorService.balanceHastings).toPlainString()} Siacoins")
                        .setPositiveButton("Close", null)
                        .show()
            }
        }

        syncBar.setProgressTextColor(MainActivity.defaultTextColor)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                walletMonitorService = (service as BaseMonitorService.LocalBinder).service as WalletMonitorService
                walletMonitorService.registerListener(this@WalletFragment)
                refreshWalletService()
                bound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                bound = false
            }
        }
        activity.bindService(Intent(activity, WalletMonitorService::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    override fun onBalanceUpdate(service: WalletMonitorService) {
        when (walletMonitorService.walletStatus) {
            WalletMonitorService.WalletStatus.NONE -> statusButton.setIcon(R.drawable.ic_add_white)
            WalletMonitorService.WalletStatus.LOCKED -> statusButton.setIcon(R.drawable.ic_lock_outline_white)
            WalletMonitorService.WalletStatus.UNLOCKED -> statusButton.setIcon(R.drawable.ic_lock_open_white)
        }
        balanceText.text = Wallet.round(Wallet.hastingsToSC(service.balanceHastings))
        balanceUnconfirmed.text = "${if (service.balanceHastingsUnconfirmed > BigDecimal.ZERO) "+" else ""}${Wallet.round(Wallet.hastingsToSC(service.balanceHastingsUnconfirmed))} unconfirmed"
//        refreshButton.actionView = null
    }

    override fun onUsdUpdate(service: WalletMonitorService) {
        balanceUsdText.text = "${Wallet.round(service.balanceUsd)} USD"
    }

    override fun onTransactionsUpdate(service: WalletMonitorService) {
        val hideZero = prefs.hideZero
        transactionExpandableGroups.clear()
        for (tx in service.transactions) {
            if (hideZero && tx.isNetZero)
                continue
            transactionExpandableGroups.add(transactionToGroupWithChild(tx))
        }
        transactionList.adapter = TransactionListAdapter(transactionExpandableGroups)
    }

    override fun onSyncUpdate(service: WalletMonitorService) {
        val syncProgress = service.syncProgress
        val height = service.blockHeight
        if (syncProgress == 100.0) {
            syncText.text = "Synced: $height"
            syncBar.progress = 100
        } else if (syncProgress == 0.0) {
            syncText.text = "Not synced"
            syncBar.progress = 0
        } else {
            syncText.text = "Syncing: $height"
            syncBar.progress = syncProgress.toInt()
        }
    }

    override fun onBalanceError(error: SiaRequest.Error) {
        error.snackbar(view)
//        refreshButton.actionView = null
    }

    override fun onUsdError(error: VolleyError) {
        Utils.snackbar(view, "Error retreiving USD value", Snackbar.LENGTH_SHORT)
    }

    override fun onTransactionsError(error: SiaRequest.Error) {
        error.snackbar(view)
    }

    override fun onSyncError(error: SiaRequest.Error) {
        error.snackbar(view)
        syncText.text = "Not synced"
        syncBar.progress = 0
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionRefresh -> refreshWalletService()
            R.id.actionStatus -> {
                when (item.icon.constantState) {
                    ContextCompat.getDrawable(activity, R.drawable.ic_add_white).constantState -> replaceExpandFrame(WalletCreateDialog())
                    ContextCompat.getDrawable(activity, R.drawable.ic_lock_outline_white).constantState -> replaceExpandFrame(WalletUnlockDialog())
                    ContextCompat.getDrawable(activity, R.drawable.ic_lock_open_white).constantState -> Wallet.lock(object : SiaRequest.VolleyCallback {
                        override fun onSuccess(response: JSONObject) {
                            refreshWalletService()
                            Utils.successSnackbar(view)
                        }

                        override fun onError(error: SiaRequest.Error) {
                            error.snackbar(view)
                        }
                    })
                }
            }
            R.id.actionUnlock -> replaceExpandFrame(WalletUnlockDialog())
            R.id.actionLock -> Wallet.lock(object : SiaRequest.VolleyCallback {
                override fun onSuccess(response: JSONObject) {
                    refreshWalletService()
                    Utils.successSnackbar(view)
                }

                override fun onError(error: SiaRequest.Error) {
                    error.snackbar(view)
                }
            })
            R.id.actionChangePassword -> replaceExpandFrame(WalletChangePasswordDialog())
            R.id.actionViewSeeds -> replaceExpandFrame(WalletSeedsDialog())
            R.id.actionCreateWallet -> replaceExpandFrame(WalletCreateDialog())
            R.id.actionSweepSeed -> replaceExpandFrame(WalletSweepSeedDialog())
            R.id.actionViewAddresses -> replaceExpandFrame(WalletAddressesDialog())
            R.id.actionAddSeed -> replaceExpandFrame(WalletAddSeedDialog())
            R.id.actionGenPaperWallet -> (activity as MainActivity).displayFragmentClass(PaperWalletFragment::class.java, "Generated paper wallet", null)
        }

        return super.onOptionsItemSelected(item)
    }

    fun refreshWalletService() {
        if (bound) {
//            refreshButton.setActionView(R.layout.refresh_progress)
            walletMonitorService.refresh()
        }
    }

    fun replaceExpandFrame(fragment: Fragment) {
        fragmentManager.beginTransaction().replace(R.id.expandFrame, fragment).commit()
        expandFrame.visibility = View.VISIBLE
    }

    private fun transactionToGroupWithChild(tx: Transaction): TransactionExpandableGroup {
        val child = ArrayList<Transaction>()
        child.add(tx)
        return TransactionExpandableGroup(tx.netValueStringRounded, tx.confirmationDate, child)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            walletMonitorService.unregisterListener(this)
            if (isAdded) {
                activity.unbindService(connection)
                bound = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu)
        refreshButton = menu.findItem(R.id.actionRefresh)
        statusButton = menu.findItem(R.id.actionStatus)
    }
}
