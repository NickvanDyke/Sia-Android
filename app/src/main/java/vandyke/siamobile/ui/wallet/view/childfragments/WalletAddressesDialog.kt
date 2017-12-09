/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view.childfragments

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_addresses.*
import vandyke.siamobile.R
import vandyke.siamobile.ui.custom.TextCopyAdapter
import vandyke.siamobile.util.observe

class WalletAddressesDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_addresses

    override fun create(view: View, savedInstanceState: Bundle?) {
        val adapter = TextCopyAdapter()

        val layoutManager = LinearLayoutManager(activity)
        addressesList.layoutManager = layoutManager
        addressesList.addItemDecoration(DividerItemDecoration(addressesList.context, layoutManager.orientation))
        addressesList.adapter = adapter

        viewModel.addresses.observe(this) {
            adapter.data = it.addresses
            adapter.notifyDataSetChanged()
        }

        viewModel.getAddresses()
    }
}
