/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view.transactionslist

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import vandyke.siamobile.R
import vandyke.siamobile.backend.data.wallet.TransactionData

class TransactionAdapter : RecyclerView.Adapter<TransactionHolder>() {
    var transactions: List<TransactionData> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.holder_transaction, parent, false)
        val holder = TransactionHolder(view)
        view.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://explorer.siahub.info/hash/${holder.transactionId.text.toString().replace("\n", "")}"))
            context?.startActivity(intent)
        }
        return holder
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

}