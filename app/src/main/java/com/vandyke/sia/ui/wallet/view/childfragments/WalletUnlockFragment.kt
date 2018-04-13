/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.vandyke.sia.R
import com.vandyke.sia.util.KeyboardUtil
import kotlinx.android.synthetic.main.fragment_wallet_unlock.*

class WalletUnlockFragment : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_unlock

    override fun create(view: View, savedInstanceState: Bundle?) {
        walletPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
                vm.unlock(walletPassword.text.toString())
            true
        }

        walletPassword.requestFocus()
        KeyboardUtil.showKeyboard(context!!)
    }

    override fun onCheckPressed(): Boolean {
        vm.unlock(walletPassword.text.toString())
        return true
    }
}