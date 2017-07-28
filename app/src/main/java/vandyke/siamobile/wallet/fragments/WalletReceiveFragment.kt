/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.fragments

import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_wallet_receive.*
import net.glxn.qrgen.android.QRCode
import org.json.JSONException
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.Wallet
import vandyke.siamobile.misc.Utils

class WalletReceiveFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_wallet_receive, null)
        walletQrCode.visibility = View.INVISIBLE

        Wallet.newAddress(object : SiaRequest.VolleyCallback {
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
        view.findViewById<View>(R.id.walletAddressCopy).setOnClickListener {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("receive address", (view.findViewById<View>(R.id.receiveAddress) as TextView).text)
            clipboard.primaryClip = clip
            Utils.snackbar(view, "Copied receive address", Snackbar.LENGTH_SHORT)
            container.visibility = View.GONE
        }
        view.findViewById<View>(R.id.walletAddressClose).setOnClickListener { container.visibility = View.GONE }

        return view
    }

    fun setQrCode(walletAddress: String) {
        walletQrCode.visibility = View.VISIBLE
        walletQrCode.setImageBitmap(QRCode.from(walletAddress).bitmap())
    }
}
