/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.os.Bundle
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.ui.common.TextCopyAdapter
import kotlinx.android.synthetic.main.fragment_wallet_seeds.*

class WalletSeedsFragment : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_seeds

    override fun create(view: View, savedInstanceState: Bundle?) {
        val adapter = TextCopyAdapter()

        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        seedsList.layoutManager = layoutManager
        seedsList.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(seedsList.context, layoutManager.orientation))
        seedsList.adapter = adapter

        vm.getSeeds().subscribe({ seeds ->
            adapter.data = seeds.map { it.seed }
            adapter.notifyDataSetChanged()
        }, {})
    }
}
