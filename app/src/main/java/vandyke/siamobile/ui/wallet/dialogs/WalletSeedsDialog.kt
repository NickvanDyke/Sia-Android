/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.ui.wallet.dialogs

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_seeds.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.Wallet
import vandyke.siamobile.ui.misc.TextTouchCopyListAdapter
import java.util.*

class WalletSeedsDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_seeds

    override fun create(view: View?, savedInstanceState: Bundle?) {
        setCloseListener(walletSeedsClose)

        val seeds = ArrayList<String>()
        val adapter = TextTouchCopyListAdapter(activity, R.layout.text_touch_copy_list_item, seeds)
        seedsList.adapter = adapter
        Wallet.seeds("english", SiaCallback({
            seeds += it.allseeds
            adapter.notifyDataSetChanged()
        }, {
            it.snackbar(view)
        }))
    }
}
