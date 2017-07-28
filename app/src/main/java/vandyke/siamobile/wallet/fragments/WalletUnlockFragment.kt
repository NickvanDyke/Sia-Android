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
import kotlinx.android.synthetic.main.fragment_wallet_unlock.*
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.Wallet
import vandyke.siamobile.backend.wallet.WalletMonitorService
import vandyke.siamobile.misc.Utils

class WalletUnlockFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View {
        val view = inflater.inflate(R.layout.fragment_wallet_unlock, null)

        walletUnlockConfirm.setOnClickListener {
            Wallet.unlock(walletPassword.text.toString(), object : SiaRequest.VolleyCallback {
                override fun onSuccess(response: JSONObject) {
                    Utils.successSnackbar(view)
                    Utils.hideSoftKeyboard(activity)
                    container.visibility = View.GONE
                    WalletMonitorService.staticRefresh()
                }

                override fun onError(error: SiaRequest.Error) {
                    if (error.reason == SiaRequest.Error.Reason.ANOTHER_WALLET_SCAN_UNDERWAY) {
                        Utils.snackbar(view, "Scanning the blockchain, please wait. Your wallet will unlock when finished", Snackbar.LENGTH_LONG)
                        Utils.hideSoftKeyboard(activity)
                        container.visibility = View.GONE
                    } else {
                        error.snackbar(view)
                    }
                }
            })
        }
        walletUnlockCancel.setOnClickListener {
            container.visibility = View.GONE
            Utils.hideSoftKeyboard(activity)
        }

        return view
    }
}
