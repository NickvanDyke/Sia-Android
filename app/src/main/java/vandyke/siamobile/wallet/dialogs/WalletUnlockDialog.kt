/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.dialogs

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_unlock.*
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.WalletApiJava
import vandyke.siamobile.backend.wallet.WalletMonitorService
import vandyke.siamobile.util.SnackbarUtil

class WalletUnlockDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_unlock

    override fun create(view: View?, savedInstanceState: Bundle?) {
        walletUnlockConfirm.setOnClickListener {
            WalletApiJava.unlock(walletPassword.text.toString(), object : SiaRequest.VolleyCallback {
                override fun onSuccess(response: JSONObject) {
                    SnackbarUtil.successSnackbar(view)
                    close()
                    WalletMonitorService.staticRefresh()
                }

                override fun onError(error: SiaRequest.Error) {
                    if (error.reason == SiaRequest.Error.Reason.WALLET_SCAN_IN_PROGRESS) {
                        SnackbarUtil.snackbar(container, "Scanning the blockchain, please wait. Your walletModel will unlock when finished", Snackbar.LENGTH_LONG)
                        close()
                    } else {
                        error.snackbar(view)
                    }
                }
            })
        }
        setCloseListener(walletUnlockCancel)
    }
}