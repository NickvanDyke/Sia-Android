/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.appComponent
import com.vandyke.sia.data.helpers.ScValueHelper
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.models.consensus.ConsensusData
import com.vandyke.sia.data.remote.WalletLocked
import com.vandyke.sia.data.siad.SiadStatus
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.wallet.view.childfragments.*
import com.vandyke.sia.ui.wallet.view.transactionslist.TransactionAdapter
import com.vandyke.sia.ui.wallet.viewmodel.WalletViewModel
import com.vandyke.sia.util.*
import com.vandyke.sia.util.rx.observe
import io.github.tonnyl.light.Light
import kotlinx.android.synthetic.main.fragment_wallet.*
import java.math.BigDecimal
import java.text.NumberFormat
import javax.inject.Inject


class WalletFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_wallet
    override val hasOptionsMenu = true
    override val title: String = "Wallet"

    @Inject
    lateinit var siadStatus: SiadStatus
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: WalletViewModel

    private val adapter = TransactionAdapter()
    private var expandedFragment: BaseWalletFragment? = null
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
                if (expandedFragment != null) {
                    if (expandedFragment?.onCheckPressed() == false)
                        collapseFrame()
                } else if (viewModel.wallet.value?.encrypted == false) {
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
        transactionListSwipe.setOnRefreshListener(viewModel::refreshAll)
        transactionListSwipe.setColors(context!!)

        expandableFrame.onSwipeUp = ::collapseFrame

        // setupChart() TODO: confirm/deny that this is working right and how I want it to
        
        /* observe VM stuff */
        viewModel.refreshing.observe(this, transactionListSwipe::setRefreshing)

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
            setFabIcon()
            setStatusIcon()
            updateUsdValue()
        }

        viewModel.scValue.observe(this) {
            updateUsdValue()
        }

        viewModel.transactions.observe(this) {
            if (it.isNotEmpty())
                Prefs.displayedTransaction = true
            adapter.update(it.filterNot { Prefs.hideZero && it.isNetZero })
        }

        viewModel.consensus.observe(this) {
            setSyncStatus()
        }

        viewModel.numPeers.observe(this) {
            setSyncStatus()
        }

        viewModel.success.observe(this) {
            Light.success(wallet_coordinator, it, Snackbar.LENGTH_SHORT).show()
            collapseFrame()
        }

        viewModel.error.observe(this) {
            it.snackbar(wallet_coordinator)
            if (it is WalletLocked)
                expandFrame(WalletUnlockDialog())
        }

        viewModel.seed.observe(this) {
            WalletCreateDialog.showSeed(it, context!!)
        }

        siadStatus.state.observe(this) {
            if (it == SiadStatus.State.SIAD_LOADED)
                viewModel.refreshAll()
        }
    }

    private fun updateUsdValue() {
        if (viewModel.wallet.value != null && viewModel.scValue.value != null)
            balanceUsdText.text = ("${viewModel.wallet.value!!.confirmedSiacoinBalance.toSC()
                .toCurrency(ScValueHelper.getValueByCurrency(Prefs.defaultCurrency, viewModel.scValue.value!!))
                .format()} ${Prefs.defaultCurrency}")
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
            else -> return false
        }
        return true
    }

    fun expandFrame(fragment: BaseWalletFragment) {
        childFragmentManager.beginTransaction().replace(R.id.expandableFrame, fragment).commit()
        childFragmentManager.executePendingTransactions()
        expandedFragment = fragment
        expandableFrame.expandVertically(fragment.height)
        setProgressColor(R.color.colorPrimary)
        setFabIcon()
    }

    fun collapseFrame() {
        expandedFragment = null
        setFabIcon()
        expandableFrame.collapseVertically({
            val currentChildFragment = childFragmentManager.findFragmentById(R.id.expandableFrame)
            if (currentChildFragment != null)
                childFragmentManager.beginTransaction().remove(currentChildFragment).commit()
            setProgressColor(android.R.color.white)
        })
        KeyboardUtil.hideKeyboard(activity!!)
    }

    override fun onShow() {
        super.onShow()
        actionBar.elevation = 0f
        viewModel.refreshAll()
    }

    override fun onHide() {
        super.onHide()
        actionBar.elevation = 12f
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu)
        statusButton = menu.findItem(R.id.actionStatus)
        setStatusIcon()
    }

    private fun setStatusIcon() {
        statusButton?.setIcon(
                when (viewModel.wallet.value?.encrypted == true || viewModel.wallet.value?.rescanning == true) {
                    false -> R.drawable.ic_add
                    true -> {
                        if (!viewModel.wallet.value!!.unlocked)
                            R.drawable.ic_lock_outline
                        else
                            R.drawable.ic_lock_open
                    }
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

    private fun setFabIcon() {
        val wallet = viewModel.wallet.value
        fabWalletMenu.menuIconView.setImageResource(when {
            expandedFragment != null -> R.drawable.ic_check
            wallet?.unlocked == false && wallet.encrypted == true -> R.drawable.ic_lock_open
            else -> R.drawable.ic_add
        })
    }

    private fun setupChart() {
//        /* set up the chart and its data set */
//        val lineDataSet = LineDataSet(null, "")
//        with(lineDataSet) {
//            setDrawCircles(false)
//            setDrawValues(false)
//            setDrawFilled(true)
//            isHighlightEnabled = false
//            color = Color.TRANSPARENT
//            fillColor = context!!.getColorRes(R.color.colorPrimaryDark)
//            /* causes a crash if the dataset is empty, so we add an empty one. Bug with the lib it seems, based off googling */
//            addEntry(Entry(0f, 0f))
//        }
//        with(siaChart) {
//            setViewPortOffsets(0f, 0f, 0f, 0f)
//            data = LineData(lineDataSet)
//            isDragEnabled = false
//            setScaleEnabled(false)
//            legend.isEnabled = false
//            description.isEnabled = false
//            setDrawGridBackground(false)
//            xAxis.isEnabled = false
//            axisLeft.isEnabled = false
//            axisRight.isEnabled = false
//            invalidate()
//        }
//
//        viewModel.walletMonthHistory.observe(this) {
//            // TODO: still not completely sure this is working as I want it to... seems to be quirky
//            lineDataSet.values = it.map { walletData ->
//                Entry(walletData.timestamp.toFloat(), walletData.confirmedSiacoinBalance.toSC().toFloat())
//            }
//            /* causes a crash if the dataset is empty, so we add an empty one. Bug with the lib it seems, based off googling */
//            if (lineDataSet.values.isEmpty())
//                lineDataSet.addEntry(Entry(0f, 0f))
//            lineDataSet.notifyDataSetChanged()
//            siaChart.data.notifyDataChanged()
//            siaChart.notifyDataSetChanged()
//            siaChart.invalidate()
//        }
    }
}
