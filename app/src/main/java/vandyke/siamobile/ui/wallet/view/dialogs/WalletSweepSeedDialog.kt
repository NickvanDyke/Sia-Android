/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view.dialogs

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_sweep.*
import vandyke.siamobile.R
import vandyke.siamobile.ui.wallet.viewmodel.WalletViewModel

class WalletSweepSeedDialog(private val viewModel: WalletViewModel? = null) : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_sweep

    override fun create(view: View?, savedInstanceState: Bundle?) {
        setCloseButton(walletSweepCancel)
        walletAddSeed.setOnClickListener {
            viewModel!!.sweep(walletSweepSeed.text.toString())
        }
    }
}
