/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.ui.wallet.view.ScannerActivity
import com.vandyke.sia.util.KeyboardUtil
import com.vandyke.sia.util.toHastings
import io.github.tonnyl.light.Light
import kotlinx.android.synthetic.main.fragment_wallet_send.*

class WalletSendFragment : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_send

    override fun create(view: View, savedInstanceState: Bundle?) {
        walletScan.setOnClickListener {
            startActivityForResult(Intent(activity, ScannerActivity::class.java), SCAN_REQUEST)
        }

        pasteClipboard.setOnClickListener {
            KeyboardUtil.getClipboardPrimaryText(context!!)?.let {
                sendRecipient.setText(it)
            } ?: Light.error(view, "No text on clipboard to paste", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == SCAN_REQUEST) {
            sendRecipient.setText(data?.getStringExtra(SCAN_RESULT_KEY))
        }
    }

    override fun onCheckPressed(): Boolean {
        val sendAmount = sendAmount.text.toString().toHastings().toPlainString()
        vm.send(sendAmount, sendRecipient.text.toString())
        return true
    }

    companion object {
        private const val SCAN_REQUEST = 20
        const val SCAN_RESULT_KEY = "SCAN_RESULT"
    }
}
