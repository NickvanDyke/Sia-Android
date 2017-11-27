/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import kotlinx.android.synthetic.main.fragment_wallet.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.siad.SiadService
import vandyke.siamobile.ui.MainActivity
import vandyke.siamobile.ui.settings.Prefs
import vandyke.siamobile.ui.wallet.view.dialogs.*
import vandyke.siamobile.ui.wallet.view.transactionslist.TransactionAdapter
import vandyke.siamobile.ui.wallet.viewmodel.WalletViewModel
import vandyke.siamobile.util.*
import java.math.BigDecimal

class WalletFragment : Fragment(), SiadService.SiadListener {

    private lateinit var viewModel: WalletViewModel

    private val adapter = TransactionAdapter()

    private var statusButton: MenuItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

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

        /* set up click listeners for the big stuff */
        sendButton.setOnClickListener { expandFrame(WalletSendDialog(viewModel)) }
        receiveButton.setOnClickListener { expandFrame(WalletReceiveDialog(viewModel.model)) }
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

        /* listen to siad output, so that we can refresh the viewModel at appropriate times */
        SiadService.addListener(this)

        /* observe data in the viewModel */
        viewModel.wallet.observe(this, {
            balanceUnconfirmed?.text = "${if (it.unconfirmedsiacoinbalance > BigDecimal.ZERO) "+" else ""}${it.unconfirmedsiacoinbalance.toSC().round().toPlainString()} unconfirmed"
            balanceText?.text = it.confirmedsiacoinbalance.toSC().round().toPlainString()
            setStatusIcon()
            transactionListSwipe?.isRefreshing = false // TODO: maybe I don't need null-safe calls anymore now that I'm using lifecycle stuff? Check later
            updateUsdValue()
        })

        viewModel.usd.observe(this, {
            updateUsdValue()
        })

        viewModel.transactions.observe(this, {
            val hideZero = Prefs.hideZero
            adapter.transactions = it.alltransactions.filterNot { hideZero && it.isNetZero }.reversed()
            adapter.notifyDataSetChanged()
        })

        viewModel.consensus.observe(this, {
            if (it.synced) {
                syncText?.text = "${getString(R.string.synced)}: ${it.height}"
                syncBar?.progress = 100
            } else {
                syncText?.text = "${getString(R.string.syncing)}: ${it.height}"
                syncBar?.progress = it.syncprogress.toInt()
            }
        })

        viewModel.success.observe(this, {
            SnackbarUtil.snackbar(view, it)
            collapseFrame()
        })

        viewModel.error.observe(this, {
            transactionListSwipe?.isRefreshing = false
            it.snackbar(view)
        })

        viewModel.seed.observe(this, {
            if (Prefs.operationMode == "cold_storage")
            WalletCreateDialog.showCsWarning(context!!)
        WalletCreateDialog.showSeed(it, context!!)
        })

        viewModel.refresh()
    }

    private fun updateUsdValue() {
        if (viewModel.wallet.value != null && viewModel.usd.value != null)
            balanceUsdText.text = "${viewModel.wallet.value!!.confirmedsiacoinbalance.toSC()
                    .toUsd(viewModel.usd.value!!.price_usd).round().toPlainString()} USD"
    }

    override fun onSiadOutput(line: String) {
        if (line.contains("Finished loading") || line.contains("Done!"))
            viewModel.refresh()
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
                    false -> expandFrame(WalletCreateDialog(viewModel))
                    true -> if (!viewModel.wallet.value!!.unlocked) expandFrame(WalletUnlockDialog(viewModel))
                    else viewModel.lock()
                }
            }
            R.id.actionUnlock -> expandFrame(WalletUnlockDialog(viewModel))
            R.id.actionLock -> viewModel.lock()
            R.id.actionChangePassword -> expandFrame(WalletChangePasswordDialog(viewModel))
            R.id.actionViewSeeds -> expandFrame(WalletSeedsDialog(viewModel.model))
            R.id.actionCreateWallet -> expandFrame(WalletCreateDialog(viewModel))
            R.id.actionSweepSeed -> expandFrame(WalletSweepSeedDialog(viewModel))
            R.id.actionViewAddresses -> expandFrame(WalletAddressesDialog(viewModel.model))
            R.id.actionGenPaperWallet -> context!!.startActivity(Intent(context, PaperWalletActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    fun expandFrame(fragment: Fragment) {
        fragmentManager!!.beginTransaction().replace(R.id.expandableFrame, fragment).commit()
        expandableFrame?.visibility = View.VISIBLE
    }

    fun collapseFrame() {
        expandableFrame.visibility = View.GONE
        GenUtil.hideSoftKeyboard(activity)
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkMode()
        viewModel.refresh()
    }

    override fun onHiddenChanged(hidden: Boolean) { // TODO: should put the invalidate part in a basefragment class
        super.onHiddenChanged(hidden)
        if (!hidden) {
            activity!!.invalidateOptionsMenu()
            viewModel.checkMode()
            viewModel.refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SiadService.removeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu)
        statusButton = menu.findItem(R.id.actionStatus)
        setStatusIcon()
    }

    fun setStatusIcon() {
        val walletData = viewModel.wallet.value
        if (walletData != null)
            when (walletData.encrypted) {
                false -> statusButton?.setIcon(R.drawable.ic_add)
                true -> if (!walletData.unlocked) statusButton?.setIcon(R.drawable.ic_lock_outline)
                else statusButton?.setIcon(R.drawable.ic_lock_open)
            }
    }

    fun onBackPressed(): Boolean { // TODO: put this in basefragment class
        if (expandableFrame.visibility == View.VISIBLE) {
            collapseFrame()
            return true
        } else {
            return false
        }
    }
}
