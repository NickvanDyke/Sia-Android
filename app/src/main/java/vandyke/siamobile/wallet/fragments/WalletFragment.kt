/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.fragments

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
import vandyke.siamobile.wallet.transactionslist.TransactionExpandableGroup
import vandyke.siamobile.wallet.transactionslist.TransactionListAdapter
import java.math.BigDecimal
import java.util.*

class WalletFragment : Fragment(), WalletMonitorService.WalletUpdateListener {

    private val transactionExpandableGroups = ArrayList<TransactionExpandableGroup>()

    private lateinit var myView: View

    private lateinit var connection: ServiceConnection
    private lateinit var walletMonitorService: WalletMonitorService
    private var bound = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        myView = inflater.inflate(R.layout.fragment_wallet, container, false)
        setHasOptionsMenu(true)
        return myView
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

        sendButton.setOnClickListener { replaceExpandFrame(WalletSendFragment()) }
        receiveButton.setOnClickListener { replaceExpandFrame(WalletReceiveFragment()) }

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

    override fun onActivityCreated(savedInstanceState: Bundle) {
        super.onActivityCreated(savedInstanceState)
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                walletMonitorService = (service as BaseMonitorService.LocalBinder).service as WalletMonitorService
                walletMonitorService.registerListener(this@WalletFragment)
                walletMonitorService.refresh()
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
            WalletMonitorService.WalletStatus.NONE -> {
                walletStatusImage.setImageResource(R.drawable.ic_add_black)
                walletStatusImage.setOnClickListener { replaceExpandFrame(WalletCreateFragment()) }
            }
            WalletMonitorService.WalletStatus.LOCKED -> {
                walletStatusImage.setImageResource(R.drawable.ic_lock_outline_black)
                walletStatusImage.setOnClickListener { replaceExpandFrame(WalletUnlockFragment()) }
            }
            WalletMonitorService.WalletStatus.UNLOCKED -> {
                walletStatusImage.setImageResource(R.drawable.ic_lock_open_black)
                walletStatusImage.setOnClickListener { view ->
                    Wallet.lock(object : SiaRequest.VolleyCallback {
                        override fun onSuccess(response: JSONObject) {
                            Utils.successSnackbar(view)
                            walletMonitorService.refresh()
                        }

                        override fun onError(error: SiaRequest.Error) {
                            error.snackbar(view)
                        }
                    })
                }
            }
        }
        balanceText.text = Wallet.round(Wallet.hastingsToSC(service.balanceHastings))
        balanceUnconfirmed.text = "${if (service.balanceHastingsUnconfirmed > BigDecimal.ZERO) "+" else ""} ${Wallet.round(Wallet.hastingsToSC(service.balanceHastingsUnconfirmed!!))} unconfirmed"
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
        transactionList!!.adapter = TransactionListAdapter(transactionExpandableGroups)
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
        error.snackbar(myView)
    }

    override fun onUsdError(error: VolleyError) {
        Utils.snackbar(myView, "Error retreiving USD value", Snackbar.LENGTH_SHORT)
    }

    override fun onTransactionsError(error: SiaRequest.Error) {
        error.snackbar(myView)
    }

    override fun onSyncError(error: SiaRequest.Error) {
        error.snackbar(myView)
        syncText.text = "Not synced"
        syncBar.progress = 0
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionRefresh -> if (bound)
                walletMonitorService.refresh()
            R.id.actionUnlock -> replaceExpandFrame(WalletUnlockFragment())
            R.id.actionLock -> Wallet.lock(object : SiaRequest.VolleyCallback {
                override fun onSuccess(response: JSONObject) {
                    Utils.successSnackbar(myView)
                }

                override fun onError(error: SiaRequest.Error) {
                    error.snackbar(myView)
                }
            })
            R.id.actionChangePassword -> replaceExpandFrame(WalletChangePasswordFragment())
            R.id.actionViewSeeds -> replaceExpandFrame(WalletSeedsFragment())
            R.id.actionCreateWallet -> replaceExpandFrame(WalletCreateFragment())
            R.id.actionSweepSeed -> replaceExpandFrame(WalletSweepSeedFragment())
            R.id.actionViewAddresses -> replaceExpandFrame(WalletAddressesFragment())
            R.id.actionAddSeed -> replaceExpandFrame(WalletAddSeedFragment())
            R.id.actionGenPaperWallet -> (activity as MainActivity).displayFragmentClass(PaperWalletFragment::class.java, "Generated paper wallet", null)
        }

        return super.onOptionsItemSelected(item)
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
    }
}
