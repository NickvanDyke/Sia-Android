/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.appComponent
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.models.consensus.ConsensusData
import com.vandyke.sia.data.remote.SiadNotReady
import com.vandyke.sia.data.remote.SiadNotRunning
import com.vandyke.sia.data.remote.WalletLocked
import com.vandyke.sia.data.siad.SiadSource
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.wallet.view.childfragments.*
import com.vandyke.sia.ui.wallet.view.transactionslist.TransactionAdapter
import com.vandyke.sia.ui.wallet.viewmodel.WalletViewModel
import com.vandyke.sia.util.*
import io.reactivex.exceptions.CompositeException
import kotlinx.android.synthetic.main.fragment_wallet.*
import java.math.BigDecimal
import java.text.NumberFormat
import javax.inject.Inject


class WalletFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_wallet
    override val hasOptionsMenu = true

    @Inject
    lateinit var siadSource: SiadSource
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: WalletViewModel

    private val adapter = TransactionAdapter()

    private var statusButton: MenuItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appComponent.inject(this)

        viewModel = ViewModelProviders.of(this, factory).get(WalletViewModel::class.java)

        /* set up recyclerview for transactions */
        transactionList.addItemDecoration(DividerItemDecoration(transactionList.context,
                (transactionList.layoutManager as LinearLayoutManager).orientation))
        transactionList.adapter = adapter

        /* set up click listeners for the big buttons */
        fabWalletMenu.setOnMenuButtonClickListener {
            if (!fabWalletMenu.isOpened) {
                if (viewModel.wallet.value?.encrypted == false) {
                    expandFrame(WalletCreateDialog())
                } else if (viewModel.wallet.value?.unlocked == false) {
                    expandFrame(WalletUnlockDialog())
                } else {
                    fabWalletMenu.open(true)
                }
            } else {
                fabWalletMenu.close(true)
            }
        }
        fabSend.setOnClickListener {
            fabWalletMenu.close(true)
            expandFrame(WalletSendDialog())
        }
        fabReceive.setOnClickListener {
            fabWalletMenu.close(true)
            expandFrame(WalletReceiveDialog())
        }
        balanceText.setOnClickListener { v ->
            AlertDialog.Builder(v.context)
                    .setTitle("Exact Balance")
                    .setMessage("${viewModel.wallet.value?.confirmedSiacoinBalance?.toSC()?.toPlainString()
                            ?: 0} Siacoins")
                    .setPositiveButton("Close", null)
                    .show()
        }

        /* set swipe-down stuff */
        transactionListSwipe.setOnRefreshListener { viewModel.refreshAll() }
        transactionListSwipe.setColorSchemeResources(R.color.colorAccent)
        val array = context!!.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        val backgroundColor = array.getColor(0, 0xFF00FF)
        array.recycle()
        transactionListSwipe.setProgressBackgroundColorSchemeColor(backgroundColor)

        expandableFrame.onSwipeUp = ::collapseFrame

        /* observe VM stuff */
        viewModel.refreshing.observe(this) {
            transactionListSwipe.isRefreshing = it
        }

        viewModel.activeTasks.observe(this) {
            // TODO: when being made visible, the bar flickers at the location it was at last, before restarting
            // Tried a few potential solutions, none worked
            progressBar.visibility = if (it > 0) View.VISIBLE else View.INVISIBLE
        }

        /* observe data in the viewModel */
        viewModel.wallet.observe(this) {
            balanceText.text = it.confirmedSiacoinBalance.toSC().format()
            if (it.unconfirmedSiacoinBalance != BigDecimal.ZERO) {
                balanceUnconfirmedText.text = ("${it.unconfirmedSiacoinBalance.toSC().format()} unconfirmed")
                balanceUnconfirmedText.visibility = View.VISIBLE
            } else {
                balanceUnconfirmedText.visibility = View.INVISIBLE
            }
            if (!it.unlocked && it.encrypted) {
                fabWalletMenu.menuIconView.setImageResource(R.drawable.ic_lock_open)
            } else {
                fabWalletMenu.menuIconView.setImageResource(R.drawable.ic_add)
            }
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
            setSyncStatus()
        }

        viewModel.numPeers.observe(this) {
            setSyncStatus()
        }

        viewModel.success.observe(this) {
            SnackbarUtil.showSnackbar(wallet_coordinator, it)
            collapseFrame()
        }

        viewModel.error.observe(this) {
            if (it is WalletLocked) {
                it.snackbar(wallet_coordinator)
                expandFrame(WalletUnlockDialog())
            } else if (siadSource.allConditionsGood.value && it is SiadNotRunning) {
                return@observe
            } else if (siadSource.allConditionsGood.value && it is CompositeException) {
                if (it.exceptions.all { e -> e is SiadNotRunning }) {
                    return@observe
                } else if (it.exceptions.any { e -> e is SiadNotReady }) {
                    SiadNotReady().snackbar(wallet_coordinator)
                    return@observe
                } else {
                    it.snackbar(wallet_coordinator)
                }
            } else {
                it.snackbar(wallet_coordinator)
            }
        }

        viewModel.seed.observe(this) {
            WalletCreateDialog.showSeed(it, context!!)
        }

        siadSource.isSiadLoaded.observe(this) {
            if (it)
                viewModel.refreshAll()
        }
    }

    private fun updateUsdValue() {
        if (viewModel.wallet.value != null && viewModel.usd.value != null)
            balanceUsdText.text = ("${viewModel.wallet.value!!.confirmedSiacoinBalance.toSC()
                    .toUsd(viewModel.usd.value!!.UsdPerSc).format()} USD")
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
        setProgressColor(R.color.colorPrimary)
    }

    fun collapseFrame() {
        expandableFrame.collapseVertically({
            val currentChildFragment = childFragmentManager.findFragmentById(R.id.expandableFrame)
            if (currentChildFragment != null)
                childFragmentManager.beginTransaction().remove(currentChildFragment).commit()
            setProgressColor(android.R.color.white)
        })
        GenUtil.hideSoftKeyboard(activity!!)
    }

    override fun onShow() {
        setActionBarElevation(0f)
        viewModel.refreshAll()
    }

    override fun onHide() {
        setActionBarElevation(12f)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu)
        statusButton = menu.findItem(R.id.actionStatus)
        setStatusIcon()
    }

    private fun setStatusIcon() {
        statusButton?.setIcon(when (viewModel.wallet.value?.encrypted ?: false || viewModel.wallet.value?.rescanning ?: false) {
            false -> R.drawable.ic_add
            true -> if (!viewModel.wallet.value!!.unlocked) R.drawable.ic_lock_outline
            else R.drawable.ic_lock_open
        })
    }

    private fun setSyncStatus() {
        val consensus = viewModel.consensus.value ?: ConsensusData(false, 0, "", BigDecimal.ZERO)
        val height = NumberFormat.getInstance().format(consensus.height)
        if (viewModel.numPeers.value == 0) {
            syncText.text = ("Not syncing: $height (${consensus.syncProgress.toInt()}%)")
        } else {
            if (consensus.synced) {
                syncText.text = ("${getString(R.string.synced)}: $height")
            } else {
                syncText.text = ("${getString(R.string.syncing)}: $height (${consensus.syncProgress.toInt()}%)")
            }
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

    private fun setProgressColor(resId: Int) {
        progressBar.indeterminateDrawable.setColorFilter(ContextCompat.getColor(context!!, resId), PorterDuff.Mode.SRC_IN)
    }

    private fun setActionBarElevation(elevation: Float) {
        (activity as AppCompatActivity).supportActionBar!!.elevation = elevation
    }
}
