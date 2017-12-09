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
import kotlinx.android.synthetic.main.fragment_wallet_seeds.*
import vandyke.siamobile.R
import vandyke.siamobile.ui.custom.TextCopyAdapter
import vandyke.siamobile.util.observe

class WalletSeedsDialog : BaseWalletFragment() {
    override val layout: Int = R.layout.fragment_wallet_seeds

    override fun create(view: View, savedInstanceState: Bundle?) {
        val adapter = TextCopyAdapter()

        val layoutManager = LinearLayoutManager(activity)
        seedsList.layoutManager = layoutManager
        seedsList.addItemDecoration(DividerItemDecoration(seedsList.context, layoutManager.orientation))
        seedsList.adapter = adapter

        viewModel.seeds.observe(this) {
            val list = mutableListOf<String>()
            list.addAll(it.allseeds)
            adapter.data = list
            adapter.notifyDataSetChanged()
        }

        viewModel.getSeeds()
    }
}
