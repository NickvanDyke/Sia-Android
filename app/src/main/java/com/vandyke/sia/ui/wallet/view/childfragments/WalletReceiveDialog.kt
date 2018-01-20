/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.SnackbarUtil
import com.vandyke.sia.util.customMsg
import kotlinx.android.synthetic.main.fragment_wallet_receive.*
import net.glxn.qrgen.android.QRCode

class WalletReceiveDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_receive

    override fun create(view: View, savedInstanceState: Bundle?) {
        viewModel.getAddress().subscribe({ address ->
            if (isVisible) {
                receiveAddress.text = address.address
                setQrCode(address.address)
            }
        }, {
            if (isVisible) {
                receiveAddress.text = it.customMsg()
            }
        })

        receiveAddress.setOnClickListener {
            GenUtil.copyToClipboard(context!!, receiveAddress.text)
            SnackbarUtil.showSnackbar(view, "Copied receive address", Snackbar.LENGTH_SHORT)
        }
    }

    private fun setQrCode(walletAddress: String) {
        walletQrCode.visibility = View.VISIBLE
        walletQrCode.setImageBitmap(QRCode.from(walletAddress).bitmap())
    }
}
