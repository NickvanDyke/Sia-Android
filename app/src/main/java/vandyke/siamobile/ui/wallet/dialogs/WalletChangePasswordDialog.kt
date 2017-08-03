/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.ui.wallet.dialogs

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_change_password.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.Wallet
import vandyke.siamobile.util.SnackbarUtil

class WalletChangePasswordDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_change_password

    override fun create(view: View?, savedInstanceState: Bundle?) {
        setCloseListener(walletChangeCancel)
        walletChange.setOnClickListener(View.OnClickListener {
            val newPassword = newPassword.text.toString()
            if (newPassword != confirmNewPassword.text.toString()) {
                SnackbarUtil.snackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT)
                return@OnClickListener
            }

            Wallet.changePassword(currentPassword.text.toString(), newPassword, SiaCallback({
                SnackbarUtil.successSnackbar(view)
                close()
            }, {
                it.snackbar(view)
            }))
        })
    }
}
