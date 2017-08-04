/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.ui.wallet

import android.app.Fragment
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.Snackbar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import kotlinx.android.synthetic.main.fragment_wallet.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.BaseMonitorService
import vandyke.siamobile.backend.coldstorage.ColdStorageHttpServer
import vandyke.siamobile.backend.models.*
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.backend.networking.Wallet
import vandyke.siamobile.backend.wallet.WalletService
import vandyke.siamobile.prefs
import vandyke.siamobile.ui.MainActivity
import vandyke.siamobile.ui.wallet.dialogs.*
import vandyke.siamobile.ui.wallet.transactionslist.TransactionAdapter
import vandyke.siamobile.util.*
import java.math.BigDecimal

class WalletFragment : Fragment(), WalletService.WalletUpdateListener {

    private val adapter = TransactionAdapter()

    private lateinit var connection: ServiceConnection
    private lateinit var walletService: WalletService
    private var bound = false

    private var statusButton: MenuItem? = null
    private var walletModel: WalletModel? = null // TODO: temp fix for tracking status to set icon
    private var balanceHastings: BigDecimal = BigDecimal.ZERO

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
        transactionList.adapter = adapter

        sendButton.setOnClickListener { replaceExpandFrame(WalletSendDialog()) }
        receiveButton.setOnClickListener { replaceExpandFrame(WalletReceiveDialog()) }

        balanceText.setOnClickListener { v ->
            if (prefs.operationMode == "cold_storage") {
                ColdStorageHttpServer.showColdStorageHelp(v.context)
            } else {
                GenUtil.getDialogBuilder(v.context)
                        .setTitle("Exact Balance")
                        .setMessage("${balanceHastings.toSC().toPlainString()} Siacoins")
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
                walletService = (service as BaseMonitorService.LocalBinder).service as WalletService
                walletService.registerListener(this@WalletFragment)
                bound = true
                refreshWalletService()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                bound = false
            }
        }
        activity.bindService(Intent(activity, WalletService::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    override fun onBalanceUpdate(walletModel: WalletModel) {
        this.balanceHastings = walletModel.confirmedsiacoinbalance
        this.walletModel = walletModel
        balanceUnconfirmed.text = "${if (walletModel.unconfirmedsiacoinbalance > BigDecimal.ZERO) "+" else ""}${walletModel.unconfirmedsiacoinbalance.toSC().round().toPlainString()} unconfirmed"
        balanceText.text = walletModel.confirmedsiacoinbalance.toSC().round().toPlainString()
        setStatusIcon()
//        refreshButton.actionView = null
    }

    override fun onUsdUpdate(scPriceModel: ScPriceModel) {
        balanceUsdText.text = "${balanceHastings.toSC().toUsd(scPriceModel.usdPrice).round().toPlainString()} USD"
    }

    override fun onTransactionsUpdate(transactionsModel: TransactionsModel) {
        val list = ArrayList<TransactionModel>()
        val hideZero = prefs.hideZero
        transactionsModel.alltransactions
                .filterNot { hideZero && it.isNetZero }
                .forEach { list.add(0, it) }
        adapter.setTransactions(list)
        adapter.notifyDataSetChanged()
    }

    override fun onSyncUpdate(consensusModel: ConsensusModel) {
        val height = consensusModel.height
        if (consensusModel.synced) {
            syncText.text = "Synced: $height"
            syncBar.progress = 100
        } else if (consensusModel.syncprogress == 0.0) {
            syncText.text = "Not synced"
            syncBar.progress = 0
        } else {
            syncText.text = "Syncing: $height"
            syncBar.progress = consensusModel.syncprogress.toInt()
        }
    }

    override fun onBalanceError(error: SiaError) {
        error.snackbar(view)
//        refreshButton.actionView = null
    }

    override fun onUsdError(error: SiaError) {
        SnackbarUtil.snackbar(view, "Error retrieving USD value", Snackbar.LENGTH_SHORT)
    }

    override fun onTransactionsError(error: SiaError) {
        error.snackbar(view)
    }

    override fun onSyncError(error: SiaError) {
        error.snackbar(view)
        syncText.text = "Not synced"
        syncBar.progress = 0
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionRefresh -> refreshWalletService()
            R.id.actionStatus -> {
                when (walletModel?.encrypted) {
                    false -> replaceExpandFrame(WalletCreateDialog())
                    true -> if (!walletModel!!.unlocked) replaceExpandFrame(WalletUnlockDialog())
                    else lockWallet()
                }
            }
            R.id.actionUnlock -> replaceExpandFrame(WalletUnlockDialog())
            R.id.actionLock -> lockWallet()
            R.id.actionChangePassword -> replaceExpandFrame(WalletChangePasswordDialog())
            R.id.actionViewSeeds -> replaceExpandFrame(WalletSeedsDialog())
            R.id.actionCreateWallet -> replaceExpandFrame(WalletCreateDialog())
            R.id.actionSweepSeed -> replaceExpandFrame(WalletSweepSeedDialog())
            R.id.actionViewAddresses -> replaceExpandFrame(WalletAddressesDialog())
//            R.id.actionAddSeed -> replaceExpandFrame(WalletAddSeedDialog())
            R.id.actionGenPaperWallet -> (activity as MainActivity).displayFragmentClass(PaperWalletFragment::class.java, "Generated paper walletModel", null)
        }

        return super.onOptionsItemSelected(item)
    }

    fun refreshWalletService() {
        if (bound) {
//            refreshButton.setActionView(R.layout.refresh_progress)
            walletService.refresh()
        }
    }

    fun replaceExpandFrame(fragment: Fragment) {
        fragmentManager.beginTransaction().replace(R.id.expandFrame, fragment).commit()
        expandFrame.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            walletService.unregisterListener(this)
            if (isAdded) {
                activity.unbindService(connection)
                bound = false
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            activity.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu)
        if (bound) {
            statusButton = menu.findItem(R.id.actionStatus)
            if (walletModel != null)
                setStatusIcon()
        }
    }

    fun setStatusIcon() {
        if (walletModel != null)
            when (walletModel!!.encrypted) {
                false -> statusButton?.setIcon(R.drawable.ic_add)
                true -> if (!walletModel!!.unlocked) statusButton?.setIcon(R.drawable.ic_lock_outline)
                else statusButton?.setIcon(R.drawable.ic_lock_open)
            }
    }

    fun lockWallet() {
        Wallet.lock(SiaCallback({ ->
            refreshWalletService()
            SnackbarUtil.successSnackbar(view)
        }, {
            it.snackbar(view)
        }))
    }
}
