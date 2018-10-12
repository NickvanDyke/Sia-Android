/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.transactionslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.vandyke.sia.R
import com.vandyke.sia.data.models.wallet.TransactionData

// should be able to disable move detection. Not sure if I can with PagedListAdapter though
class TransactionAdapter : PagedListAdapter<TransactionData, TransactionHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        return TransactionHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_transaction, parent, false))
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        holder.bind(getItem(position) ?: return)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionData>() {
            override fun areItemsTheSame(oldItem: TransactionData, newItem: TransactionData): Boolean {
                return oldItem.transactionid == newItem.transactionid
            }

            override fun areContentsTheSame(oldItem: TransactionData, newItem: TransactionData): Boolean {
                return oldItem.transactionid == newItem.transactionid
                        && oldItem.confirmationDate == newItem.confirmationDate
                        && oldItem.netValue == newItem.netValue
            }
        }
    }
}