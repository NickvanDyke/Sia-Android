/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.ui.common.TextCopyAdapter
import kotlinx.android.synthetic.main.fragment_wallet_seeds.*

class WalletSeedsDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_seeds

    override fun create(view: View, savedInstanceState: Bundle?) {
        val adapter = TextCopyAdapter()

        val layoutManager = LinearLayoutManager(activity)
        seedsList.layoutManager = layoutManager
        seedsList.addItemDecoration(DividerItemDecoration(seedsList.context, layoutManager.orientation))
        seedsList.adapter = adapter

        viewModel.getSeeds().subscribe({ seeds ->
            val list = mutableListOf<String>()
            list.addAll(seeds.allseeds)
            adapter.data = list
            adapter.notifyDataSetChanged()
        }, {})
    }
}
