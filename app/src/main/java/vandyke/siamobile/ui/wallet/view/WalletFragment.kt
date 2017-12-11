/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet.*
import vandyke.siamobile.R
import vandyke.siamobile.data.local.Prefs
import vandyke.siamobile.data.remote.SiaError
import vandyke.siamobile.ui.BaseFragment
import vandyke.siamobile.ui.main.MainActivity
import vandyke.siamobile.ui.wallet.view.childfragments.*
import vandyke.siamobile.ui.wallet.view.transactionslist.TransactionAdapter
import vandyke.siamobile.ui.wallet.viewmodel.WalletViewModel
import vandyke.siamobile.util.*
import java.math.BigDecimal

class WalletFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_wallet
    override val hasOptionsMenu = true

    private lateinit var viewModel: WalletViewModel

    private val adapter = TransactionAdapter()

    private var statusButton: MenuItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /* color stuff depending on theme */
        if (Prefs.darkMode) {
            top_shadow.setBackgroundResource(R.drawable.top_shadow_dark)
        }
        syncBar.setProgressTextColor(MainActivity.defaultTextColor)

        viewModel = ViewModelProviders.of(this).get(WalletViewModel::class.java)

        /* set up recyclerview for transactions */
        val layoutManager = LinearLayoutManager(activity)
        transactionList.layoutManager = layoutManager
        transactionList.addItemDecoration(DividerItemDecoration(transactionList.context, layoutManager.orientation))
        transactionList.adapter = adapter

        /* set up click listeners for the big buttons */
        sendButton.setOnClickListener { expandFrame(WalletSendDialog()) }
        receiveButton.setOnClickListener { expandFrame(WalletReceiveDialog()) }
        balanceText.setOnClickListener { v ->
            AlertDialog.Builder(v.context)
                    .setTitle("Exact Balance")
                    .setMessage("${viewModel.wallet.value!!.confirmedsiacoinbalance.toSC().toPlainString()} Siacoins")
                    .setPositiveButton("Close", null)
                    .show()
        }

        /* set listener to refresh the viewModel when the swipelayout is triggered */
        transactionListSwipe.setOnRefreshListener { viewModel.refresh() }
        transactionListSwipe.setColorSchemeResources(R.color.colorAccent)

        viewModel.activeTasks.observe(this) {
            progress.visibility = if (it > 0) View.VISIBLE else View.GONE
            if (it == 0)
                transactionListSwipe?.isRefreshing = false
        }

        /* observe data in the viewModel */
        viewModel.wallet.observe(this) {
            balanceUnconfirmed?.text = ((if (it.unconfirmedsiacoinbalance > BigDecimal.ZERO) "+" else "") +
                    "${it.unconfirmedsiacoinbalance.toSC().round().toPlainString()} unconfirmed")
            balanceText?.text = it.confirmedsiacoinbalance.toSC().round().toPlainString()
            setStatusIcon()
            updateUsdValue()
        }

        viewModel.usd.observe(this) {
            updateUsdValue()
        }

        viewModel.transactions.observe(this) {
            val hideZero = Prefs.hideZero
            adapter.transactions = it.alltransactions.filterNot { hideZero && it.isNetZero }.reversed()
            adapter.notifyDataSetChanged()
        }

        viewModel.consensus.observe(this) {
            if (it.synced) {
                syncText?.text = ("${getString(R.string.synced)}: ${it.height}")
                syncBar?.progress = 100
            } else {
                syncText?.text = ("${getString(R.string.syncing)}: ${it.height}")
                syncBar?.progress = it.syncprogress.toInt()
            }
        }

        viewModel.success.observe(this) {
            SnackbarUtil.snackbar(view, it)
            collapseFrame()
        }

        viewModel.error.observe(this) {
            it.snackbar(view)
            if (it.reason == SiaError.Reason.WALLET_LOCKED)
                expandFrame(WalletUnlockDialog())
        }

        viewModel.seed.observe(this) {
            if (Prefs.operationMode == "cold_storage")
                WalletCreateDialog.showCsWarning(context!!)
            WalletCreateDialog.showSeed(it, context!!)
        }
    }

    private fun updateUsdValue() {
        if (viewModel.wallet.value != null && viewModel.usd.value != null)
            balanceUsdText.text = ("${viewModel.wallet.value!!.confirmedsiacoinbalance.toSC()
                    .toUsd(viewModel.usd.value!!.price_usd).round().toPlainString()} USD")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionStatus -> {
//                when (statusButton?.icon?.constantState) {
//                    activity.resources.getDrawable(R.drawable.ic_add, null).constantState -> expandFrame(WalletCreateDialog(viewModel))
//                    activity.resources.getDrawable(R.drawable.ic_lock_outline, null).constantState -> expandFrame(WalletUnlockDialog(viewModel))
//                    activity.resources.getDrawable(R.drawable.ic_lock_open, null).constantState -> viewModel.lock()
//                }
                when (viewModel.wallet.value?.encrypted) {
                    false, null -> expandFrame(WalletCreateDialog())
                    true -> if (!viewModel.wallet.value!!.unlocked) expandFrame(WalletUnlockDialog())
                    else viewModel.lock()
                }
            }
            R.id.actionUnlock -> expandFrame(WalletUnlockDialog())
            R.id.actionLock -> viewModel.lock()
            R.id.actionChangePassword -> expandFrame(WalletChangePasswordDialog())
            R.id.actionViewSeeds -> expandFrame(WalletSeedsDialog())
            R.id.actionCreateWallet -> expandFrame(WalletCreateDialog())
            R.id.actionSweepSeed -> expandFrame(WalletSweepSeedDialog())
            R.id.actionViewAddresses -> expandFrame(WalletAddressesDialog())
            R.id.actionGenPaperWallet -> context!!.startActivity(Intent(context, PaperWalletActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    fun expandFrame(fragment: BaseWalletFragment) {
        childFragmentManager.beginTransaction().replace(R.id.expandableFrame, fragment).commit()
        childFragmentManager.executePendingTransactions()
        expandableFrame.expand(fragment.height)
    }

    fun collapseFrame() {
        expandableFrame.collapse()
    }

    override fun onShow() {
        viewModel.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu)
        statusButton = menu.findItem(R.id.actionStatus)
        setStatusIcon()
    }

    private fun setStatusIcon() {
        val walletData = viewModel.wallet.value
        if (walletData != null)
            when (walletData.encrypted) {
                false -> statusButton?.setIcon(R.drawable.ic_add)
                true -> if (!walletData.unlocked) statusButton?.setIcon(R.drawable.ic_lock_outline)
                else statusButton?.setIcon(R.drawable.ic_lock_open)
            }
    }

    override fun onBackPressed(): Boolean {
        if (expandableFrame.height != 0) {
            collapseFrame()
            return true
        } else {
            return false
        }
    }
}
