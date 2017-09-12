/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view.dialogs

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_receive.*
import net.glxn.qrgen.android.QRCode
import vandyke.siamobile.R
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.ui.wallet.model.IWalletModel
import vandyke.siamobile.util.GenUtil
import vandyke.siamobile.util.SnackbarUtil

class WalletReceiveDialog(private val model: IWalletModel? = null) : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_receive

    override fun create(view: View?, savedInstanceState: Bundle?) {
        walletQrCode.visibility = View.INVISIBLE
        setCloseButton(walletReceiveClose)

        model?.getAddress(SiaCallback({ it ->
            SnackbarUtil.successSnackbar(view)
            if (isVisible) {
                receiveAddress.text = it.address
                setQrCode(it.address)
            }
        }, {
            it.snackbar(view)
            if (isVisible)
                receiveAddress.text = "${it.reason.msg}\n"
        }))

        walletAddressCopy.setOnClickListener {
            GenUtil.copyToClipboard(activity, receiveAddress.text)
            SnackbarUtil.snackbar(view, "Copied receive address", Snackbar.LENGTH_SHORT)
            close()
        }
    }

    fun setQrCode(walletAddress: String) {
        walletQrCode.visibility = View.VISIBLE
        walletQrCode.setImageBitmap(QRCode.from(walletAddress).bitmap())
    }
}