/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.dialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_send.*
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.Wallet
import vandyke.siamobile.misc.Utils
import vandyke.siamobile.prefs
import vandyke.siamobile.wallet.ScannerActivity
import java.math.BigDecimal
import java.math.RoundingMode

class WalletSendDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_send

    override fun create(view: View?, savedInstanceState: Bundle?) {
        if (prefs.feesEnabled)
            walletSendFee.visibility = View.GONE
        sendAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (sendAmount.text.toString() == "")
                    walletSendFee.text = "0.5% App fee: 0.000"
                else
                    walletSendFee.text = "0.5% App fee: ${BigDecimal(s.toString()).multiply(Utils.devFee).setScale(3, RoundingMode.FLOOR).toPlainString()} SC"
            }
            override fun afterTextChanged(s: Editable) {}
        })
        walletSend.setOnClickListener {
            val sendAmount = Wallet.scToHastings(sendAmount.text.toString())
            if (prefs.feesEnabled)
                Wallet.sendSiacoinsWithDevFee(sendAmount,
                        sendRecipient.text.toString(),
                        object : SiaRequest.VolleyCallback {
                            override fun onSuccess(response: JSONObject) {
                                Utils.successSnackbar(view)
                                close()
                            }

                            override fun onError(error: SiaRequest.Error) {
                                error.snackbar(view)
                            }
                        })
            else
                Wallet.sendSiacoins(sendAmount,
                        sendRecipient.text.toString(),
                        object : SiaRequest.VolleyCallback {
                            override fun onSuccess(response: JSONObject) {
                                Utils.successSnackbar(view)
                                close()
                            }

                            override fun onError(error: SiaRequest.Error) {
                                error.snackbar(view)
                            }
                        })
        }
        setCloseListener(walletSendCancel)

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
