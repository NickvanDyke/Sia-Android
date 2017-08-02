/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.dialogs

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_sweep.*
import vandyke.siamobile.R
import vandyke.siamobile.api.networking.SiaCallback
import vandyke.siamobile.api.networking.Wallet
import vandyke.siamobile.util.SnackbarUtil

class WalletSweepSeedDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_sweep

    override fun create(view: View?, savedInstanceState: Bundle?) {
        setCloseListener(walletSweepCancel)
        walletAddSeed.setOnClickListener {
            Wallet.sweep("english", walletSweepSeed.text.toString(), SiaCallback({
                SnackbarUtil.successSnackbar(view)
                close()
            }, {
                it.snackbar(view)
            }))
        }
    }
}
