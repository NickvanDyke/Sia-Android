/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.dialogs

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_unlock.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.backend.networking.Wallet
import vandyke.siamobile.backend.wallet.WalletService
import vandyke.siamobile.util.SnackbarUtil

class WalletUnlockDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_unlock

    override fun create(view: View?, savedInstanceState: Bundle?) {
        walletUnlockConfirm.setOnClickListener {

            Wallet.unlock(walletPassword.text.toString(), SiaCallback({ ->
                SnackbarUtil.successSnackbar(view)
                close()
                WalletService.singleAction(activity, { it.refresh() })
            }, {
                if (it.reason == SiaError.Reason.WALLET_SCAN_IN_PROGRESS) {
                    SnackbarUtil.snackbar(container, "Scanning the blockchain, please wait. Your wallet will unlock when finished", Snackbar.LENGTH_LONG)
                    close()
                } else {
                    it.snackbar(view)
                }
            }))
        }
        setCloseListener(walletUnlockCancel)
    }
}