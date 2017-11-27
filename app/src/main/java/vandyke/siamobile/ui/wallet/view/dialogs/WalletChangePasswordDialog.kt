/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view.dialogs

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_change_password.*
import vandyke.siamobile.R
import vandyke.siamobile.ui.wallet.viewmodel.WalletViewModel
import vandyke.siamobile.util.SnackbarUtil

class WalletChangePasswordDialog(private val viewModel: WalletViewModel? = null) : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_change_password

    override fun create(view: View?, savedInstanceState: Bundle?) {
        setCloseButton(walletChangeCancel)

        walletChange.setOnClickListener(View.OnClickListener {
            val newPassword = newPassword.text.toString()
            if (newPassword != confirmNewPassword.text.toString()) {
                SnackbarUtil.snackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT)
                return@OnClickListener
            }

            viewModel!!.changePassword(currentPassword.text.toString(), newPassword)
        })
    }
}
