/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.dialogs

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_addresses.*
import vandyke.siamobile.R
import vandyke.siamobile.api.networking.SiaCallback
import vandyke.siamobile.api.networking.Wallet
import vandyke.siamobile.misc.TextTouchCopyListAdapter
import java.util.*

class WalletAddressesDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_addresses

    override fun create(view: View?, savedInstanceState: Bundle?) {
        val addresses = ArrayList<String>()
        val adapter = TextTouchCopyListAdapter(activity, R.layout.text_touch_copy_list_item, addresses)
        addressesList.adapter = adapter

        Wallet.addresses(SiaCallback({
            addresses += it.addresses
            adapter.notifyDataSetChanged()
        }, {
            it.snackbar(view)
        }))

        setCloseListener(walletAddressesCancel)
    }
}
