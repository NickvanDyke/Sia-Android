/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.ui.wallet.dialogs

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_add_seed.*
import vandyke.siamobile.R

class WalletAddSeedDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_add_seed

    override fun create(view: View?, savedInstanceState: Bundle?) {
//        walletAddSeed.setOnClickListener {
//            WalletApiJava.seed(walletPassword.text.toString(), "english",
//                    walletAddSeed.text.toString(),
//                    object : SiaRequest.VolleyCallback {
//                        override fun onSuccess(response: JSONObject) {
//                            SnackbarUtil.successSnackbar(view)
//                            close()
//                        }
//
//                        override fun onError(error: SiaRequest.Error) {
//                            error.snackbar(view)
//                        }
//                    })
//        }
        setCloseListener(walletAddSeedCancel)
    }
}
