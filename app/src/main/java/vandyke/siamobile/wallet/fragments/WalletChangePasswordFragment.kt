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
import kotlinx.android.synthetic.main.fragment_wallet_change_password.*
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.Wallet
import vandyke.siamobile.misc.Utils

class WalletChangePasswordFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_wallet_change_password, null)
        walletChange.setOnClickListener(View.OnClickListener {
            val newPassword = newPassword.text.toString()
            if (newPassword != confirmNewPassword.text.toString()) {
                Utils.snackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT)
                return@OnClickListener
            }
            Wallet.changePassword(currentPassword.text.toString(),
                    newPassword, object : SiaRequest.VolleyCallback {
                override fun onSuccess(response: JSONObject) {
                    Utils.successSnackbar(view)
                    container.visibility = View.GONE
                    Utils.hideSoftKeyboard(activity)
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
}
