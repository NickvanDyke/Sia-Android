/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import com.vandyke.sia.R
import kotlinx.android.synthetic.main.fragment_wallet_sweep.*

class WalletSweepSeedDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_sweep

    override fun onCheckPressed(): Boolean {
        viewModel.sweep(walletSweepSeed.text.toString())
        return true
    }
}
