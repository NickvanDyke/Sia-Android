/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.ui.wallet.dialogs

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_addresses.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.Wallet
import vandyke.siamobile.ui.misc.TextTouchCopyListAdapter
import java.util.*

class WalletAddressesDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_addresses

    override fun create(view: View?, savedInstanceState: Bundle?) {
        val addresses = ArrayList<String>()
        val adapter = TextTouchCopyListAdapter(activity, R.layout.list_item_text_touch_copy, addresses)
        addressesList.adapter = adapter

        Wallet.addresses(SiaCallback({ it ->
            addresses += it.addresses
            adapter.notifyDataSetChanged()
        }, {
            it.snackbar(view)
        }))

        setCloseListener(walletAddressesCancel)
    }
}
