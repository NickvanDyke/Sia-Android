/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.os.Bundle
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.ui.common.TextCopyAdapter
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_wallet_addresses.*

class WalletAddressesFragment : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_addresses

    private lateinit var subscription: Disposable

    override fun create(view: View, savedInstanceState: Bundle?) {
        val adapter = TextCopyAdapter()

        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        addressesList.layoutManager = layoutManager
        addressesList.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(addressesList.context, layoutManager.orientation))
        addressesList.adapter = adapter

        vm.getAddresses().subscribe({ addresses ->
            adapter.data = addresses.map { it.address }
            adapter.notifyDataSetChanged()
        }, {})
    }
}
