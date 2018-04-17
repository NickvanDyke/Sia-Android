/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
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
import com.vandyke.sia.data.siad.SiadStatus
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.common.RecyclerViewHideFabOnScrollListener
import com.vandyke.sia.ui.wallet.view.childfragments.*
import com.vandyke.sia.ui.wallet.view.transactionslist.TransactionAdapter
import com.vandyke.sia.ui.wallet.viewmodel.WalletViewModel
import com.vandyke.sia.util.*
import com.vandyke.sia.util.rx.observe
import io.github.tonnyl.light.Light
import it.sephiroth.android.library.tooltip.Tooltip
import it.sephiroth.android.library.tooltip.Tooltip.Gravity
import kotlinx.android.synthetic.main.fragment_wallet.*
import net.cachapa.expandablelayout.ExpandableLayout
import java.math.BigDecimal
import javax.inject.Inject


class WalletFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_wallet
    override val hasOptionsMenu = true
    override val title = "Wallet"

    @Inject
    lateinit var siadStatus: SiadStatus
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var vm: WalletViewModel

    private var childFragment: BaseWalletFragment? = null
    private var fragmentToBeExpanded: BaseWalletFragment? = null
    private var statusButton: MenuItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context!!.getAppComponent().inject(this)

        vm = ViewModelProviders.of(this, factory).get(WalletViewModel::class.java)

        /* set up recyclerview for transactions */
        val adapter = TransactionAdapter()
        transaction_list.addItemDecoration(DividerItemDecoration(transaction_list.context,
                (transaction_list.layoutManager as LinearLayoutManager).orientation))
        transaction_list.adapter = adapter
        transaction_list.addOnScrollListener(RecyclerViewHideFabOnScrollListener(fab_wallet_menu))

        /* set up click listeners for the big buttons */
        fab_wallet_menu.setOnMenuButtonClickListener {
            if (!fab_wallet_menu.isOpened) {
                when {
                    childFragment != null -> if (childFragment?.onCheckPressed() == false) collapseFrame()
                    vm.wallet.value?.encrypted != true -> expandFrame(WalletCreateFragment())
                    vm.wallet.value?.unlocked != true -> expandFrame(WalletUnlockFragment())
                    else -> fab_wallet_menu.open(true)
                }
            } else {
                fab_wallet_menu.close(true)
            }
        }
        fab_send.setOnClickListener {
            fab_wallet_menu.close(true)
            expandFrame(WalletSendFragment())
        }
        fab_receive.setOnClickListener {
            fab_wallet_menu.close(true)
            expandFrame(WalletReceiveFragment())
        }
        balance_text.setOnClickListener { v ->
            AlertDialog.Builder(v.context)
                    .setTitle("Exact Balance")
                    .setMessage("${vm.wallet.value?.confirmedsiacoinbalance?.toSC()?.toPlainString()
                            ?: 0} Siacoins")
                    .setPositiveButton("Close", null)
                    .show()
        }

        /* set swipe-down stuff */
        transaction_list_swipe.setOnRefreshListener(vm::refreshAll)
        transaction_list_swipe.setColors(context!!)

        expandableFrame.setOnExpansionUpdateListener { _, state ->
            when (state) {
                ExpandableLayout.State.COLLAPSED -> progress_bar?.setIndeterminateColorRes(android.R.color.white)
                else -> progress_bar?.setIndeterminateColorAttrRes(R.attr.colorPrimary)
            }

            when (state) {
                ExpandableLayout.State.COLLAPSED -> {
                    childFragment = null
                    if (fragmentToBeExpanded != null) {
                        replaceExpandedFragment(fragmentToBeExpanded!!)
                        expandableFrame.expand(true)
                    } else {
                        childFragmentManager.findFragmentById(R.id.expandableFrame)?.let {
                            childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
                        }
                    }
                }
                ExpandableLayout.State.COLLAPSING -> {
                    childFragment = null
                    KeyboardUtil.hideKeyboard(activity!!)
                    updateFabIcon()
                }
                ExpandableLayout.State.EXPANDING -> updateFabIcon()
            }
        }

        /* observe VM stuff */
        vm.refreshing.observe(this, transaction_list_swipe::setRefreshing)

        vm.activeTasks.observe(this) {
            // TODO: when being made visible, the bar flickers at the location it was at last, before restarting
            // Tried a few potential solutions, none worked
            progress_bar.goneUnless(it > 0)
        }

        /* observe data in the viewModel */
        vm.wallet.observe(this) {
            balance_text.text = it.confirmedsiacoinbalance.toSC().format()
            if (it.unconfirmedsiacoinbalance != BigDecimal.ZERO) {
                balance_unconfirmed_text.text =
                        "${if (it.unconfirmedsiacoinbalance > BigDecimal.ZERO) "+" else ""}" +
                        "${it.unconfirmedsiacoinbalance.toSC().format()} unconfirmed"
                balance_unconfirmed_text.visible()
                balance_unconfirmed_text.oneTimeTooltip("Transactions will generally confirm within a couple blocks", Gravity.BOTTOM)?.let { queueTooltip(it) }
            } else {
                balance_unconfirmed_text.invisible()
            }
            updateFabIcon()
            updateFiatValue()
        }

        vm.scValue.observe(this) {
            updateFiatValue()
        }

        vm.transactions.observe(this) {
            adapter.submitList(it)
            if (it.isNotEmpty())
                Prefs.displayedTransaction = true
        }

        vm.consensus.observe(this) {
            updateSyncStatus()
        }

        vm.numPeers.observe(this) {
            updateSyncStatus()
        }

        vm.success.observe(this) {
            Light.success(wallet_coordinator, it, Snackbar.LENGTH_SHORT).show()
            collapseFrame()
        }

        vm.error.observe(this) {
            it.snackbar(wallet_coordinator, siadStatus.state.value!!)
            if (it is WalletLocked)
                expandFrame(WalletUnlockFragment())
        }

        vm.seed.observe(this) {
            WalletCreateFragment.showSeed(it, context!!)
        }

        siadStatus.state.observe(this) {
            if (it == SiadStatus.State.SIAD_LOADED)
                vm.refreshAll()
        }

        siadStatus.mostRecentSiadOutput.observe(this) {
            val str = "Wallet: scanned to height "
            if (it.contains(str)) {
                try {
                    val height = it.substring(str.length, it.indexOf('.')).toInt()
                    unlocking_text.text = "Unlocking: ${height.format()}"
                    unlocking_text.visible()
                } catch (e: NumberFormatException) {
                    unlocking_text.gone()
                }
            } else if (it == "Done!" || it.contains("Finished loading")) {
                unlocking_text.gone()
            }
        }

        updateFabIcon()

        sync_text.oneTimeTooltip("The Sia node will initially have to download, process, and store the " +
                "Sia blockchain, which is about 11GB. This can take a while, and unfortunately is " +
                "a current limitation of the Sia software.", Tooltip.Gravity.BOTTOM)?.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionUnlock -> expandFrame(WalletUnlockFragment())
            R.id.actionLock -> vm.lock()
            R.id.actionChangePassword -> expandFrame(WalletChangePasswordFragment())
            R.id.actionViewSeeds -> expandFrame(WalletSeedsFragment())
            R.id.actionCreateWallet -> expandFrame(WalletCreateFragment())
            R.id.actionSweepSeed -> expandFrame(WalletSweepSeedFragment())
            R.id.actionViewAddress -> expandFrame(WalletReceiveFragment())
            R.id.actionViewAddresses -> expandFrame(WalletAddressesFragment())
            else -> return false
        }
        return true
    }

    private fun expandFrame(fragment: BaseWalletFragment) {
        if (expandableFrame.state != ExpandableLayout.State.COLLAPSED) {
            fragmentToBeExpanded = fragment
            expandableFrame.collapse(true)
        } else {
            replaceExpandedFragment(fragment)
            expandableFrame.expand(true)
        }
    }

    private fun collapseFrame() {
        expandableFrame.collapse(true)
    }

    private fun replaceExpandedFragment(fragment: BaseWalletFragment) {
        childFragmentManager.beginTransaction().replace(R.id.expandableFrame, fragment).commitAllowingStateLoss()
        fragmentToBeExpanded = null
        childFragment = fragment
    }

    override fun onShow() {
        actionBar.elevation = 0f
    }

    override fun onHide() {
        actionBar.elevation = 12f
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu)
    }

    private fun updateFiatValue() {
        val scBalance = vm.wallet.value?.confirmedsiacoinbalance?.toSC() ?: return
        val scValue = vm.scValue.value?.get(Prefs.fiatCurrency) ?: return
        val fiatValue = scBalance * scValue
        balance_usd_text.text = ("${fiatValue.format()} ${Prefs.fiatCurrency}")
    }

    private fun updateSyncStatus() {
        val consensus = vm.consensus.value
        val height = consensus?.height?.format() ?: 0
        val progress = consensus?.syncProgress?.toInt() ?: 0
        if (vm.numPeers.value == 0) {
            sync_text.text = ("Not syncing: $height ($progress%)")
        } else {
            if (consensus?.synced == true) {
                sync_text.text = ("${getString(R.string.synced)}: $height")
            } else {
                sync_text.text = ("${getString(R.string.syncing)}: $height ($progress%)")
            }
        }
    }

    private fun updateFabIcon() {
        val wallet = vm.wallet.value
        fab_wallet_menu.menuIconView.setImageResource(when {
            childFragment != null -> R.drawable.ic_check_white
            wallet == null || !wallet.encrypted -> R.drawable.ic_account_balance_wallet_white
            !wallet.unlocked -> R.drawable.ic_lock_open_white
            else -> R.drawable.ic_add_white
        })
    }

    override fun onBackPressed(): Boolean {
        return when {
            expandableFrame.state != ExpandableLayout.State.COLLAPSED -> {
                collapseFrame()
                true
            }
            fab_wallet_menu.isOpened -> {
                fab_wallet_menu.close(true)
                true
            }
            else -> false
        }
    }
}
