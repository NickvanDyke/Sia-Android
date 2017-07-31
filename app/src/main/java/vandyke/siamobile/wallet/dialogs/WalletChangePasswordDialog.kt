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
import kotlinx.android.synthetic.main.fragment_wallet_change_password.*
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.WalletApiJava
import vandyke.siamobile.util.SnackbarUtil

class WalletChangePasswordDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_change_password

    override fun create(view: View?, savedInstanceState: Bundle?) {
        walletChange.setOnClickListener(View.OnClickListener {
            val newPassword = newPassword.text.toString()
            if (newPassword != confirmNewPassword.text.toString()) {
                SnackbarUtil.snackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT)
                return@OnClickListener
            }
            WalletApiJava.changePassword(currentPassword.text.toString(),
                    newPassword, object : SiaRequest.VolleyCallback {
                override fun onSuccess(response: JSONObject) {
                    SnackbarUtil.successSnackbar(view)
                    close()
                }

                override fun onError(error: SiaRequest.Error) {
                    error.snackbar(view)
                }
            })
        })
        setCloseListener(walletChangeCancel)
    }
}
