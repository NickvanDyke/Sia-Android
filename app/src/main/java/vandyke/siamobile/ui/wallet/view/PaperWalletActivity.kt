/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_wallet_paper.*
import siawallet.Wallet
import vandyke.siamobile.R
import vandyke.siamobile.ui.misc.TextCopyAdapter
import vandyke.siamobile.util.GenUtil
import vandyke.siamobile.util.SnackbarUtil

class PaperWalletActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_paper)

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val wallet = Wallet()

        try {
            wallet.generateSeed()
        } catch (e: Exception) {
            e.printStackTrace()
            paperSeed.text = "Failed to generate seed"
            return
        }

        val seed = wallet.seed
        val addresses = mutableListOf<String>()
        for (i in 0..19) {
            addresses.add(wallet.getAddress(i.toLong()))
        }

        paperSeed.setOnClickListener { v ->
            GenUtil.copyToClipboard(this, seed)
            SnackbarUtil.snackbar(v, "Copied seed to clipboard", Snackbar.LENGTH_SHORT)
        }
        paperSeed.text = seed

        val layoutManager = LinearLayoutManager(this)
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

            GenUtil.copyToClipboard(this, result.toString())
            SnackbarUtil.snackbar(v, "Copied seed and addresses to clipboard", Snackbar.LENGTH_SHORT)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }
}
