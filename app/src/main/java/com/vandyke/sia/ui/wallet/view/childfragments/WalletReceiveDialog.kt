/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.util.KeyboardUtil
import com.vandyke.sia.util.customMsg
import io.github.tonnyl.light.Light
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
            KeyboardUtil.copyToClipboard(context!!, receiveAddress.text)
            Light.info(view, "Copied receive address", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setQrCode(walletAddress: String) {
        walletQrCode.visibility = View.VISIBLE
        walletQrCode.setImageBitmap(QRCode.from(walletAddress).bitmap())
    }
}
