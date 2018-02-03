/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.support.design.widget.Snackbar
import com.vandyke.sia.R
import com.vandyke.sia.util.SnackbarUtil
import kotlinx.android.synthetic.main.fragment_wallet_change_password.*

class WalletChangePasswordDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_change_password

    override fun onCheckPressed(): Boolean {
        val newPassword = newPassword.text.toString()
        if (newPassword != confirmNewPassword.text.toString()) {
            SnackbarUtil.showSnackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT)
            return true
        }

        viewModel.changePassword(currentPassword.text.toString(), newPassword)
        return true
    }
}
