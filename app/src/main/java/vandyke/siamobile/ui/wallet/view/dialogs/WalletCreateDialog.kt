/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view.dialogs

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_wallet_create.*
import vandyke.siamobile.R
import vandyke.siamobile.ui.wallet.presenter.IWalletPresenter
import vandyke.siamobile.util.GenUtil
import vandyke.siamobile.util.SnackbarUtil

class WalletCreateDialog(private val presenter: IWalletPresenter? = null) : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_create

    override fun create(view: View?, savedInstanceState: Bundle?) {
        setCloseButton(walletCreateCancel)

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
            if (!walletCreateFromSeed.isChecked) {
                presenter?.create(password, force)
            } else {
                presenter?.create(password, force, walletCreateSeed.text.toString())
            }
        })
    }


    companion object {
        fun showCsWarning(activity: Activity) {
            GenUtil.getDialogBuilder(activity)
                    .setTitle("IMPORTANT")
                    .setMessage("You just created a wallet while in cold storage mode. While in cold storage mode," +
                            " Sia Mobile is not connected to the Sia network and does not have a copy of the Sia blockchain. Normally this would mean you can't " +
                            "view your balance and transactions, but Sia Mobile ESTIMATES your CONFIRMED balance and transactions using explore.sia.tech." +
                            "\n\nIt also means certain functions that require the blockchain or a connection to the network will" +
                            " be unavailable - most importantly, you can't send coins from a cold storage wallet. If you wish to use these unavailable functions," +
                            " you can, AT ANY TIME, load your wallet seed on a full node (such as Sia-UI for desktop, or Sia Mobile in local full node mode)" +
                            " to access and interact with your previously received coins.")
                    .setPositiveButton("I have read and understood this", null)
                    .show()
        }

        fun showSeed(seed: String, activity: Activity) {
            val msg = "Below is your wallet seed. Your wallet's addresses are generated using this seed. Therefore, any coins you " +
                    "send to this wallet and its addresses will \"belong\" to this seed. It's what you will need" +
                    " in order to recover your coins if something happens to your wallet, or to load your wallet on another device. Record it elsewhere, and keep it safe."
            GenUtil.getDialogBuilder(activity)
                    .setTitle("Wallet seed")
                    .setMessage("$msg\n\n$seed")
                    .setPositiveButton("Copy seed", { _, _ ->
                        GenUtil.copyToClipboard(activity, seed)
                        Toast.makeText(activity, "Copied seed", Toast.LENGTH_SHORT).show()
                    })
                    .show()
        }
    }
}
