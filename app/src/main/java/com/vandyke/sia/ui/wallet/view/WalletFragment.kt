/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.remote.WalletLocked
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.wallet.view.childfragments.*
import com.vandyke.sia.ui.wallet.view.transactionslist.TransactionAdapter
import com.vandyke.sia.ui.wallet.viewmodel.WalletViewModel
import com.vandyke.sia.util.*
import kotlinx.android.synthetic.main.fragment_wallet.*
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
        syncBar.setProgressTextColor(balanceUsdText.currentTextColor)

        viewModel = ViewModelProviders.of(this).get(WalletViewModel::class.java)

        /* set up recyclerview for transactions */
        transactionList.addItemDecoration(DividerItemDecoration(transactionList.context,
                (transactionList.layoutManager as LinearLayoutManager).orientation))
        transactionList.adapter = adapter

        /* set up click listeners for the big buttons */
        sendButton.setOnClickListener { expandFrame(WalletSendDialog()) }
        receiveButton.setOnClickListener { expandFrame(WalletReceiveDialog()) }
        balanceText.setOnClickListener { v ->
            AlertDialog.Builder(v.context)
                    .setTitle("Exact Balance")
                    .setMessage("${viewModel.wallet.value?.confirmedSiacoinBalance?.toSC()?.toPlainString() ?: 0} Siacoins")
                    .setPositiveButton("Close", null)
                    .show()
        }

        /* set listener to updateDatabase the viewModel when the swipelayout is triggered */
        transactionListSwipe.setOnRefreshListener { viewModel.refreshAll() }
        transactionListSwipe.setColorSchemeResources(R.color.colorAccent)
        val array = context!!.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        val backgroundColor = array.getColor(0, 0xFF00FF)
        array.recycle()
        transactionListSwipe.setProgressBackgroundColorSchemeColor(backgroundColor)

        expandableFrame.onSwipeUp = ::collapseFrame

        viewModel.refreshing.observe(this) {
            transactionListSwipe.isRefreshing = it
        }

        viewModel.activeTasks.observe(this) {
            progress.visibility = if (it > 0) View.VISIBLE else View.GONE
        }

        /* observe data in the viewModel */
        viewModel.wallet.observe(this) {
            balanceUnconfirmed.text = ((if (it.unconfirmedSiacoinBalance > BigDecimal.ZERO) "+" else "") +
                    "${it.unconfirmedSiacoinBalance.toSC().round().toPlainString()} unconfirmed")
            balanceText.text = it.confirmedSiacoinBalance.toSC().round().toPlainString()
            setStatusIcon()
            updateUsdValue()
        }

        viewModel.usd.observe(this) {
            updateUsdValue()
        }

        viewModel.transactions.observe(this) {
            adapter.update(it.filterNot { Prefs.hideZero && it.isNetZero })
        }

        viewModel.consensus.observe(this) {
            if (viewModel.numPeers.value == 0) {
                syncText.text = ("Not syncing: ${it.height}")
                syncBar.progress = it.syncProgress.toInt()
            } else {
                if (it.synced) {
                    syncText.text = ("${getString(R.string.synced)}: ${it.height}")
                    syncBar.progress = 100
                } else {
                    syncText.text = ("${getString(R.string.syncing)}: ${it.height}")
                    syncBar.progress = it.syncProgress.toInt()
                }
            }
        }

        viewModel.numPeers.observe(this) {
            if (it == 0)
                syncText.text = ("Not syncing: ${viewModel.consensus.value?.height ?: 0}")
        }

        viewModel.success.observe(this) {
            SnackbarUtil.showSnackbar(view, it)
            collapseFrame()
        }

        viewModel.error.observe(this) {
            it.snackbar(view)
            if (it is WalletLocked)
                expandFrame(WalletUnlockDialog())
        }

        viewModel.seed.observe(this) {
            WalletCreateDialog.showSeed(it, context!!)
        }
    }

    private fun updateUsdValue() {
        if (viewModel.wallet.value != null && viewModel.usd.value != null)
            balanceUsdText.text = ("${viewModel.wallet.value!!.confirmedSiacoinBalance.toSC()
                    .toUsd(viewModel.usd.value!!.UsdPerSc).round().toPlainString()} USD")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionStatus -> {
                when (viewModel.wallet.value?.encrypted ?: false || viewModel.wallet.value?.rescanning ?: false) {
                    false -> expandFrame(WalletCreateDialog())
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
        }

        return super.onOptionsItemSelected(item)
    }

    fun expandFrame(fragment: BaseWalletFragment) {
        childFragmentManager.beginTransaction().replace(R.id.expandableFrame, fragment).commit()
        childFragmentManager.executePendingTransactions()
        expandableFrame.expandVertically(fragment.height)
    }

    fun collapseFrame() {
        expandableFrame.collapseVertically({
            val currentChildFragment = childFragmentManager.findFragmentById(R.id.expandableFrame)
            if (currentChildFragment != null)
                childFragmentManager.beginTransaction().remove(currentChildFragment).commit()
        })
        GenUtil.hideSoftKeyboard(activity!!)
    }

    override fun onShow() {
        viewModel.refreshAll()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu)
        statusButton = menu.findItem(R.id.actionStatus)
        setStatusIcon()
    }

    private fun setStatusIcon() {
        when (viewModel.wallet.value?.encrypted ?: false || viewModel.wallet.value?.rescanning ?: false) {
            false -> statusButton?.setIcon(R.drawable.ic_add)
            true -> if (!viewModel.wallet.value!!.unlocked) statusButton?.setIcon(R.drawable.ic_lock_outline)
            else statusButton?.setIcon(R.drawable.ic_lock_open)
        }
    }

    override fun onBackPressed(): Boolean {
        return if (expandableFrame.height != 0) {
            collapseFrame()
            true
        } else {
            false
        }
    }
}
