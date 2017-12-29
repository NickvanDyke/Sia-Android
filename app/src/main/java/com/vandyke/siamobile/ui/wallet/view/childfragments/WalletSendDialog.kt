/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.ui.wallet.view.childfragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.vandyke.siamobile.R
import com.vandyke.siamobile.ui.wallet.view.ScannerActivity
import com.vandyke.siamobile.util.toHastings
import kotlinx.android.synthetic.main.fragment_wallet_send.*

class WalletSendDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_send

    override fun create(view: View, savedInstanceState: Bundle?) {
        sendAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // change miner fee text
            }
            override fun afterTextChanged(s: Editable) {}
        })

        walletSend.setOnClickListener {
            val sendAmount = sendAmount.text.toString().toHastings().toPlainString()
            viewModel.send(sendAmount, sendRecipient.text.toString())
        }

        walletScan.setOnClickListener { startScannerActivity() }
    }

    private fun startScannerActivity() {
        startActivityForResult(Intent(activity, ScannerActivity::class.java), SCAN_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == SCAN_REQUEST) {
            sendRecipient.setText(data?.getStringExtra(SCAN_RESULT_KEY))
        }
    }

    companion object {
        private val SCAN_REQUEST = 20
        val SCAN_RESULT_KEY = "SCAN_RESULT"
    }
}
