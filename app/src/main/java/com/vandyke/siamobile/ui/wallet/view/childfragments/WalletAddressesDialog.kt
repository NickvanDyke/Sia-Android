/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.ui.wallet.view.childfragments

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.vandyke.siamobile.R
import com.vandyke.siamobile.ui.common.TextCopyAdapter
import com.vandyke.siamobile.util.observe
import kotlinx.android.synthetic.main.fragment_wallet_addresses.*

class WalletAddressesDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_addresses

    override fun create(view: View, savedInstanceState: Bundle?) {
        val adapter = TextCopyAdapter()

        val layoutManager = LinearLayoutManager(activity)
        addressesList.layoutManager = layoutManager
        addressesList.addItemDecoration(DividerItemDecoration(addressesList.context, layoutManager.orientation))
        addressesList.adapter = adapter

        viewModel.addresses.observe(this) {
            adapter.data = it.map { it.address }
            adapter.notifyDataSetChanged()
        }

        viewModel.getAddresses()
    }
}
