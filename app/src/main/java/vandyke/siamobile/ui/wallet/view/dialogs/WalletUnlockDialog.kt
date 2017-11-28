/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view.dialogs

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_unlock.*
import vandyke.siamobile.R

class WalletUnlockDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_unlock

    override fun create(view: View?, savedInstanceState: Bundle?) {
        walletUnlockConfirm.setOnClickListener {
            viewModel.unlock(walletPassword.text.toString())
        }
        setCloseButton(walletUnlockCancel)
    }
}