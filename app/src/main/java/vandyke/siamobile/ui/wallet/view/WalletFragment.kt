/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import kotlinx.android.synthetic.main.fragment_wallet.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.coldstorage.ColdStorageHttpServer
import vandyke.siamobile.backend.data.consensus.ConsensusData
import vandyke.siamobile.backend.data.wallet.ScPriceData
import vandyke.siamobile.backend.data.wallet.TransactionData
import vandyke.siamobile.backend.data.wallet.TransactionsData
import vandyke.siamobile.backend.data.wallet.WalletData
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.backend.siad.SiadService
import vandyke.siamobile.prefs
import vandyke.siamobile.ui.MainActivity
import vandyke.siamobile.ui.wallet.model.IWalletModel
import vandyke.siamobile.ui.wallet.model.WalletModelHttp
import vandyke.siamobile.ui.wallet.presenter.IWalletPresenter
import vandyke.siamobile.ui.wallet.presenter.WalletPresenter
import vandyke.siamobile.ui.wallet.view.dialogs.*
import vandyke.siamobile.ui.wallet.view.transactionslist.TransactionAdapter
import vandyke.siamobile.util.*
import java.math.BigDecimal

class WalletFragment : Fragment(), IWalletView, SiadService.SiadListener {

    private val model: IWalletModel = WalletModelHttp()
    private val presenter: IWalletPresenter = WalletPresenter(this, model)

    private val adapter = TransactionAdapter()

    private var statusButton: MenuItem? = null
    private var walletData: WalletData? = null // TODO: temp fix for tracking status to set icon
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

        sendButton.setOnClickListener { replaceExpandFrame(WalletSendDialog(presenter)) }
        receiveButton.setOnClickListener { replaceExpandFrame(WalletReceiveDialog(model)) }

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

        SiadService.addListener(this)
    }

    override fun onSuccess() {
        SnackbarUtil.successSnackbar(view)
    }

    override fun onWalletUpdate(walletData: WalletData) {
        this.balanceHastings = walletData.confirmedsiacoinbalance
        this.walletData = walletData
        balanceUnconfirmed.text = "${if (walletData.unconfirmedsiacoinbalance > BigDecimal.ZERO) "+" else ""}${walletData.unconfirmedsiacoinbalance.toSC().round().toPlainString()} unconfirmed"
        balanceText.text = walletData.confirmedsiacoinbalance.toSC().round().toPlainString()
        setStatusIcon()
//        refreshButton.actionView = null
    }

    override fun onUsdUpdate(scPriceData: ScPriceData) {
        balanceUsdText.text = "${balanceHastings.toSC().toUsd(scPriceData.price_usd).round().toPlainString()} USD"
    }

    override fun onTransactionsUpdate(transactionsData: TransactionsData) {
        val list = ArrayList<TransactionData>()
        val hideZero = prefs.hideZero
        transactionsData.alltransactions
                .filterNot { hideZero && it.isNetZero }
                .forEach { list.add(0, it) }
        adapter.setTransactions(list)
        adapter.notifyDataSetChanged()
    }

    override fun onConsensusUpdate(consensusData: ConsensusData) {
        val height = consensusData.height
        if (consensusData.synced) {
            syncText.text = "Synced: $height"
            syncBar.progress = 100
        } else if (consensusData.syncprogress == 0.0) {
            syncText.text = "Not synced"
            syncBar.progress = 0
        } else {
            syncText.text = "Syncing: $height"
            syncBar.progress = consensusData.syncprogress.toInt()
        }
    }

    override fun onError(error: SiaError) {
        when {
            error.reason == SiaError.Reason.WALLET_SCAN_IN_PROGRESS ->
                SnackbarUtil.snackbar(view, "Scanning the blockchain, please wait. This can take a while.", Snackbar.LENGTH_LONG)
            else -> error.snackbar(view)
        }
    }

    override fun onWalletError(error: SiaError) {
        error.snackbar(view)
//        refreshButton.actionView = null
    }

    override fun onUsdError(error: SiaError) {
        SnackbarUtil.snackbar(view, "Error retrieving USD value", Snackbar.LENGTH_SHORT)
    }

    override fun onTransactionsError(error: SiaError) {
        error.snackbar(view)
    }

    override fun onConsensusError(error: SiaError) {
        error.snackbar(view)
        syncText.text = "Not synced"
        syncBar.progress = 0
    }

    override fun onSiadOutput(line: String) {
        if (line.contains("Finished loading") || line.contains("Done!"))
            presenter.refresh()
    }

    override fun onWalletCreated(seed: String) {
        if (prefs.operationMode == "cold_storage")
            WalletCreateDialog.showCsWarning(activity)
        WalletCreateDialog.showSeed(seed, activity)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionRefresh -> presenter.refresh()
            R.id.actionStatus -> {
                when (walletData?.encrypted) {
                    false -> replaceExpandFrame(WalletCreateDialog(presenter))
                    true -> if (!walletData!!.unlocked) replaceExpandFrame(WalletUnlockDialog(presenter))
                    else presenter.lock()
                }
            }
            R.id.actionUnlock -> replaceExpandFrame(WalletUnlockDialog(presenter))
            R.id.actionLock -> presenter.lock()
            R.id.actionChangePassword -> replaceExpandFrame(WalletChangePasswordDialog(presenter))
            R.id.actionViewSeeds -> replaceExpandFrame(WalletSeedsDialog(model))
            R.id.actionCreateWallet -> replaceExpandFrame(WalletCreateDialog(presenter))
            R.id.actionSweepSeed -> replaceExpandFrame(WalletSweepSeedDialog(presenter))
            R.id.actionViewAddresses -> replaceExpandFrame(WalletAddressesDialog(model))
            R.id.actionGenPaperWallet -> (activity as MainActivity).displayFragmentClass(PaperWalletFragment::class.java, "Generated paper wallet", null)
        }

        return super.onOptionsItemSelected(item)
    }

    fun replaceExpandFrame(fragment: Fragment) {
        fragmentManager.beginTransaction().replace(R.id.expandFrame, fragment).commit()
        expandFrame.visibility = View.VISIBLE
    }

    override fun closeExpandableFrame() {
        expandFrame.visibility = View.GONE
        GenUtil.hideSoftKeyboard(activity)
    }

    override fun onResume() {
        super.onResume()
        presenter.refresh()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            activity.invalidateOptionsMenu()
            presenter.refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SiadService.removeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu)
        statusButton = menu.findItem(R.id.actionStatus)
        if (walletData != null)
            setStatusIcon()
    }

    fun setStatusIcon() {
        if (walletData != null)
            when (walletData!!.encrypted) {
                false -> statusButton?.setIcon(R.drawable.ic_add)
                true -> if (!walletData!!.unlocked) statusButton?.setIcon(R.drawable.ic_lock_outline)
                else statusButton?.setIcon(R.drawable.ic_lock_open)
            }
    }
}
