/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.wallet.view.transactionslist

import android.content.Intent
import android.net.Uri
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import vandyke.siamobile.R
import vandyke.siamobile.data.models.wallet.TransactionData

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

    fun update(txs: List<TransactionData>) {
        val diffResult = DiffUtil.calculateDiff(TxDiffUtil(transactions, txs))
        transactions = txs
        diffResult.dispatchUpdatesTo(this)
    }

    inner class TxDiffUtil(private val oldList: List<TransactionData>, private val newList: List<TransactionData>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition].transactionId == newList[newItemPosition].transactionId

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return old.transactionId == new.transactionId
                    && old.confirmationDate == new.confirmationDate
                    && old.netValue == new.netValue
        }
    }
}