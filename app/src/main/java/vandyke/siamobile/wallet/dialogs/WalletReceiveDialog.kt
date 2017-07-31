/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_receive.*
import net.glxn.qrgen.android.QRCode
import org.json.JSONException
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.WalletApiJava
import vandyke.siamobile.misc.Utils

class WalletReceiveDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_receive

    override fun create(view: View?, savedInstanceState: Bundle?) {
        walletQrCode.visibility = View.INVISIBLE

        WalletApiJava.newAddress(object : SiaRequest.VolleyCallback {
            override fun onSuccess(response: JSONObject) {
                try {
                    Utils.successSnackbar(view)
                    receiveAddress.text = response.getString("address")
                    setQrCode(response.getString("address"))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onError(error: SiaRequest.Error) {
                error.snackbar(view)
                receiveAddress.text = "${error.msg}\n"
            }
        })
        walletAddressCopy.setOnClickListener {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("receive address", receiveAddress.text)
            clipboard.primaryClip = clip
            Utils.snackbar(view, "Copied receive address", Snackbar.LENGTH_SHORT)
            close()
        }
        setCloseListener(walletReceiveClose)
    }

    fun setQrCode(walletAddress: String) {
        walletQrCode.visibility = View.VISIBLE
        walletQrCode.setImageBitmap(QRCode.from(walletAddress).bitmap())
    }
}
