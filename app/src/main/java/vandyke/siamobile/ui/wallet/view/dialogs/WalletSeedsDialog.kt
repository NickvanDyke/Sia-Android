/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view.dialogs

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_seeds.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.ui.misc.TextCopyAdapter
import vandyke.siamobile.ui.wallet.model.IWalletModel
import java.util.*

class WalletSeedsDialog(private val walletModel: IWalletModel? = null) : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_seeds

    override fun create(view: View?, savedInstanceState: Bundle?) {
        setCloseButton(walletSeedsClose)

        val seeds = ArrayList<String>()
        val adapter = TextCopyAdapter(seeds)

        val layoutManager = LinearLayoutManager(activity)
        seedsList.layoutManager = layoutManager
        seedsList.addItemDecoration(DividerItemDecoration(seedsList.context, layoutManager.orientation))
        seedsList.adapter = adapter

        walletModel?.getSeeds("english", SiaCallback({ it ->
            seeds += it.allseeds
            adapter.notifyDataSetChanged()
        }, {
            it.snackbar(view)
        }))
    }
}
