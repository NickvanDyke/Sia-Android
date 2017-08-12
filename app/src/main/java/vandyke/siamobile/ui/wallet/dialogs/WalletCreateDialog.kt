/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.dialogs

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_wallet_create.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.backend.networking.Wallet
import vandyke.siamobile.backend.wallet.WalletService
import vandyke.siamobile.prefs
import vandyke.siamobile.util.GenUtil
import vandyke.siamobile.util.SnackbarUtil

class WalletCreateDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_create

    override fun create(view: View?, savedInstanceState: Bundle?) {
        setCloseListener(walletCreateCancel)
        walletCreateSeed.visibility = View.GONE
        walletCreateFromSeed.setOnClickListener {
            if (walletCreateFromSeed.isChecked)
                walletCreateSeed.visibility = View.VISIBLE
            else
                walletCreateSeed.visibility = View.GONE
        }

        walletCreateForceWarning.visibility = View.GONE
        walletCreateForce.setOnClickListener {
            if (walletCreateForce.isChecked)
                walletCreateForceWarning.visibility = View.VISIBLE
            else
                walletCreateForceWarning.visibility = View.GONE
        }

        walletCreateButton.setOnClickListener(View.OnClickListener {
            val password = newPasswordCreate.text.toString()
            if (password != confirmNewPasswordCreate.text.toString()) {
                SnackbarUtil.snackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT)
                return@OnClickListener
            }
            val force = walletCreateForce.isChecked
            val dictionary = "english"
            if (!walletCreateFromSeed.isChecked) {
                Wallet.init(password, dictionary, force, SiaCallback({ it ->
                    SnackbarUtil.successSnackbar(view)
                    close()
                    WalletService.singleAction(activity, { it.refresh() })
                    if (prefs.operationMode == "cold_storage")
                        showCsWarning()
                    showSeed(it.primaryseed)
                }, {
                    if (it.reason == SiaError.Reason.WALLET_SCAN_IN_PROGRESS) {
                        SnackbarUtil.snackbar(view, "Success. Scanning the blockchain for coins belonging to the given seed. Please wait", Snackbar.LENGTH_LONG)
                        close()
                    } else {
                        it.snackbar(view)
                    }
                }))
            } else {
                Wallet.initSeed(password, dictionary, walletCreateSeed.text.toString(), force, SiaCallback({ ->
                    SnackbarUtil.successSnackbar(view)
                    close()
                    WalletService.singleAction(activity, { it.refresh() })
                    if (prefs.operationMode == "cold_storage")
                        showCsWarning()
                }, {
                    if (it.reason == SiaError.Reason.WALLET_SCAN_IN_PROGRESS) {
                        SnackbarUtil.snackbar(view, "Success. Scanning the blockchain for coins belonging to the given seed. Please wait", Snackbar.LENGTH_LONG)
                        close()
                    } else {
                        it.snackbar(view)
                    }
                }))
            }
        })
    }

    private fun showCsWarning() {
        GenUtil.getDialogBuilder(activity)
                .setTitle("IMPORTANT")
                .setMessage("You just created a wallet while in cold storage mode. While in cold storage mode," +
                        " Sia Mobile is not connected to the Sia network and does not have a copy of the Sia blockchain. Normally this would mean you can't " +
                        "view your balance and transactions, but Sia Mobile ESTIMATES your CONFIRMED balance and transactions once per minute using explore.sia.tech." +
                        "\n\nIt also means certain functions that require the blockchain or a connection to the network will" +
                        " be unavailable - most importantly, you can't send coins from a cold storage wallet. If you wish to use these unavailable functions," +
                        " you can, AT ANY TIME, load your wallet seed on a full node (such as Sia-UI for desktop, or Sia Mobile in local full node mode)" +
                        " to access and interact with your previously received coins.")
                .setPositiveButton("I have read and understood this", null)
                .show()
    }

    private fun showSeed(seed: String) {
        val msg = "Below is your wallet seed. Your wallet's addresses are generated using this seed. Therefore, any coins you " +
                "send to this wallet and its addresses will \"belong\" to this seed. It's what you will need" +
                " in order to recover your coins if something happens to your wallet, or to load your wallet on another device. Keep it safe."
        GenUtil.getDialogBuilder(activity)
                .setTitle("Wallet seed")
                .setMessage("$msg\n\n$seed")
                .setPositiveButton("Copy seed", { _, _ ->
                    GenUtil.copyToClipboard(activity, seed)
                    Toast.makeText(activity, "Copied seed", Toast.LENGTH_SHORT).show()
                })
                .setNegativeButton("Close", null)
                .show()
    }
}
