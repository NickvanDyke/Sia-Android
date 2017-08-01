/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.dialogs

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_create.*
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.WalletApiJava
import vandyke.siamobile.backend.wallet.WalletMonitorService
import vandyke.siamobile.prefs
import vandyke.siamobile.util.GenUtil
import vandyke.siamobile.util.SnackbarUtil

class WalletCreateDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_create

    override fun create(view: View?, savedInstanceState: Bundle?) {
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
            if (walletCreateFromSeed.isChecked)
                WalletApiJava.initSeed(password, force, dictionary, walletCreateSeed.text.toString(), object : SiaRequest.VolleyCallback {
                    override fun onSuccess(response: JSONObject) {
                        SnackbarUtil.successSnackbar(view)
                        close()
                        WalletMonitorService.staticRefresh()
                        if (prefs.operationMode == "cold_storage")
                            showDialog()
                    }

                    override fun onError(error: SiaRequest.Error) {
                        if (error.reason == SiaRequest.Error.Reason.WALLET_SCAN_IN_PROGRESS) {
                            SnackbarUtil.snackbar(view, "Success. Scanning the blockchain for coins belonging to the given seed. Please wait", Snackbar.LENGTH_LONG)
                            close()
                            WalletMonitorService.staticRefresh()
                        } else {
                            error.snackbar(view)
                        }
                    }
                })
            else
                WalletApiJava.init(password, force, dictionary, object : SiaRequest.VolleyCallback {
                    override fun onSuccess(response: JSONObject) {
                        SnackbarUtil.successSnackbar(view)
                        close()
                        WalletMonitorService.staticRefresh()
                        if (prefs.operationMode == "cold_storage")
                            showDialog()
                    }

                    override fun onError(error: SiaRequest.Error) {
                        error.snackbar(view)
                    }
                })
        })
        setCloseListener(walletCreateCancel)
    }

    private fun showDialog() {
        GenUtil.getDialogBuilder(activity)
                .setTitle("IMPORTANT")
                .setMessage("You just created a wallet while in cold storage mode. While in cold storage mode," +
                        " Sia Mobile is not connected to the Sia network and does not have a copy of the Sia blockchain. This means it cannot show your correct balance or transactions." +
                        " You can send coins to any of the addresses of your cold storage wallet, and at any time in the future, load your wallet seed" +
                        " on a full node (such as Sia-UI on your computer or Sia Mobile's full node mode), and have access to your previously sent coins.")
                .setPositiveButton("I have read and understood this", null)
                .show()
    }
}
