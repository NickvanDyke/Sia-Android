/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.util.SnackbarUtil
import kotlinx.android.synthetic.main.fragment_wallet_change_password.*

class WalletChangePasswordDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_change_password

    override fun create(view: View, savedInstanceState: Bundle?) {
        walletChange.setOnClickListener(View.OnClickListener {
            val newPassword = newPassword.text.toString()
            if (newPassword != confirmNewPassword.text.toString()) {
                SnackbarUtil.snackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT)
                return@OnClickListener
            }

            viewModel.changePassword(currentPassword.text.toString(), newPassword)
        })
    }
}
