/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.fragments

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import kotlinx.android.synthetic.main.fragment_wallet_create.*
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.Wallet
import vandyke.siamobile.backend.wallet.WalletMonitorService
import vandyke.siamobile.misc.Utils
import vandyke.siamobile.prefs

class WalletCreateFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_wallet_create, null)
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
                walletCreateForce.visibility = View.VISIBLE
            else
                walletCreateForce.visibility = View.GONE
        }

        walletCreateButton.setOnClickListener(View.OnClickListener {
            val password = (view.findViewById<View>(R.id.newPasswordCreate) as EditText).text.toString()
            if (newPasswordCreate.text != confirmNewPasswordCreate.text) {
                Utils.snackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT)
                return@OnClickListener
            }
            val force = walletCreateForce.isChecked
            val dictionary = "english"
            if (walletCreateFromSeed.isChecked)
                Wallet.initSeed(password, force, dictionary, walletCreateSeed.text.toString(), object : SiaRequest.VolleyCallback {
                    override fun onSuccess(response: JSONObject) {
                        Utils.successSnackbar(view)
                        container.visibility = View.GONE
                        Utils.hideSoftKeyboard(activity)
                        WalletMonitorService.staticRefresh()
                        if (prefs.operationMode == "cold_storage")
                            showDialog()
                    }

                    override fun onError(error: SiaRequest.Error) {
                        if (error.reason == SiaRequest.Error.Reason.ANOTHER_WALLET_SCAN_UNDERWAY) {
                            Utils.snackbar(view, "Success. Scanning the blockchain for coins belonging to the given seed. Please wait - it can take a while", Snackbar.LENGTH_LONG)
                            container.visibility = View.GONE
                            Utils.hideSoftKeyboard(activity)
                            WalletMonitorService.staticRefresh()
                        } else {
                            error.snackbar(view)
                        }
                    }
                })
            else
                Wallet.init(password, force, dictionary, object : SiaRequest.VolleyCallback {
                    override fun onSuccess(response: JSONObject) {
                        Utils.successSnackbar(view)
                        container.visibility = View.GONE
                        Utils.hideSoftKeyboard(activity)
                        WalletMonitorService.staticRefresh()
                        if (prefs.operationMode == "cold_storage")
                            showDialog()
                    }

                    override fun onError(error: SiaRequest.Error) {
                        error.snackbar(view)
                    }
                })
        })

        walletCreateCancel.setOnClickListener {
            container.visibility = View.GONE
            Utils.hideSoftKeyboard(activity)
        }
        return view
    }

    private fun showDialog() {
        Utils.getDialogBuilder(activity)
                .setTitle("IMPORTANT")
                .setMessage("You just created a wallet while in cold storage mode. While in cold storage mode," +
                        " Sia Mobile is not connected to the Sia network and does not have a copy of the Sia blockchain. This means it cannot show your correct balance or transactions." +
                        " You can send coins to any of the addresses of your cold storage wallet, and at any time in the future, load your wallet seed" +
                        " on a full node (such as Sia-UI on your computer or Sia Mobile's full node mode), and have access to your previously sent coins.")
                .setPositiveButton("I have read and understood this", null)
                .show()
    }
}
