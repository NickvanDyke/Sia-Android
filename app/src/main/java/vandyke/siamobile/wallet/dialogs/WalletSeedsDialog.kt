/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.dialogs

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_wallet_seeds.*
import org.json.JSONException
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.api.SiaRequest
import vandyke.siamobile.api.Wallet
import vandyke.siamobile.misc.TextTouchCopyListAdapter
import java.util.*

class WalletSeedsDialog : BaseDialogFragment() {
    override val layout: Int = R.layout.fragment_wallet_seeds

    override fun create(view: View?, savedInstanceState: Bundle?) {
        val seeds = ArrayList<String>()
        val adapter = TextTouchCopyListAdapter(activity, R.layout.text_touch_copy_list_item, seeds)
        seedsList.adapter = adapter
        Wallet.seeds("english", object : SiaRequest.VolleyCallback {
            override fun onSuccess(response: JSONObject) {
                try {
                    val seedsJson = response.getJSONArray("allseeds")
                    for (i in 0..seedsJson.length() - 1)
                        seeds.add(seedsJson.getString(i))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                adapter.notifyDataSetChanged()
            }

            override fun onError(error: SiaRequest.Error) {
                error.snackbar(view)
            }
        })
        setCloseListener(walletSeedsClose)
    }
}
