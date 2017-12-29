/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.ui.wallet.view.childfragments

import android.os.Bundle
import android.view.View
import com.vandyke.siamobile.R
import kotlinx.android.synthetic.main.fragment_wallet_sweep.*

class WalletSweepSeedDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_sweep

    override fun create(view: View, savedInstanceState: Bundle?) {
        walletAddSeed.setOnClickListener {
            viewModel.sweep(walletSweepSeed.text.toString())
        }
    }
}
