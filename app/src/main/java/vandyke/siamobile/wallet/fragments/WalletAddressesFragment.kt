/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.fragments

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_wallet_addresses.*
import org.json.JSONException
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.Wallet
import vandyke.siamobile.misc.TextTouchCopyListAdapter
import java.util.*

class WalletAddressesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_wallet_addresses, null)
        val addresses = ArrayList<String>()
        val adapter = TextTouchCopyListAdapter(activity, R.layout.text_touch_copy_list_item, addresses)
        addressesList.adapter = adapter
        Wallet.addresses(object : SiaRequest.VolleyCallback {
            override fun onSuccess(response: JSONObject) {
                try {
                    val addressesJson = response.getJSONArray("addresses")
                    for (i in 0..addressesJson.length() - 1)
                        addresses.add(addressesJson.getString(i))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                adapter.notifyDataSetChanged()
            }

            override fun onError(error: SiaRequest.Error) {
                error.snackbar(view)
            }
        })
        walletCreateCancel.setOnClickListener { container.visibility = View.GONE }
        return view
    }
}
