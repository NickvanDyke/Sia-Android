/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.dialogs

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_wallet_paper.*
import siawallet.Wallet
import vandyke.siamobile.R
import vandyke.siamobile.ui.misc.TextCopyAdapter
import vandyke.siamobile.util.GenUtil
import vandyke.siamobile.util.SnackbarUtil
import java.util.*

class PaperWalletFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_wallet_paper, null)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val wallet = Wallet()

        try {
            wallet.generateSeed()
        } catch (e: Exception) {
            e.printStackTrace()
            paperSeed.text = "Failed to generate seed"
            return
        }

        val seed = wallet.seed
        val addresses = ArrayList<String>()
        for (i in 0..19) {
            addresses.add(wallet.getAddress(i.toLong()))
        }

        paperSeed.setOnClickListener { v ->
            GenUtil.copyToClipboard(activity, seed)
            SnackbarUtil.snackbar(v, "Copied seed to clipboard", Snackbar.LENGTH_SHORT)
        }
        paperSeed.text = seed

        val layoutManager = LinearLayoutManager(activity)
        paperAddresses.layoutManager = layoutManager
        paperAddresses.addItemDecoration(DividerItemDecoration(paperAddresses.context, layoutManager.orientation))
        paperAddresses.adapter = TextCopyAdapter(addresses)

        paperCopy.setOnClickListener { v ->
            val result = StringBuilder()
            result.append("Seed:\n")
            result.append(seed + "\n")
            result.append("Addresses:\n")
            for (i in addresses.indices) {
                result.append(addresses[i])
                if (i < addresses.size - 1)
                    result.append(",\n")
            }

            GenUtil.copyToClipboard(activity, result.toString())
            SnackbarUtil.snackbar(v, "Copied seed and addresses to clipboard", Snackbar.LENGTH_SHORT)
        }
    }
}
