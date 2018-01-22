/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.transactionslist

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.vandyke.sia.R
import com.vandyke.sia.data.models.wallet.TransactionData
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.format
import com.vandyke.sia.util.toSC
import java.text.SimpleDateFormat
import java.util.*

class TransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val transactionStatus: TextView = itemView.findViewById(R.id.transactionStatus)
    private val transactionId: TextView = itemView.findViewById(R.id.transactionId)
    private val transactionValue: TextView = itemView.findViewById(R.id.transactionValue)

    fun bind(transaction: TransactionData) {
        if (!transaction.confirmed) {
            transactionStatus.text = "Unconfirmed"
            transactionStatus.setTextColor(getColor(R.color.negativeTransaction))
        } else {
            transactionStatus.text = df.format(transaction.confirmationDate)
            transactionStatus.setTextColor(transactionId.currentTextColor)
        }

        transactionId.text = transaction.transactionId

        var valueText = transaction.netValue.toSC().format()
        transactionValue.setTextColor(when {
            transaction.isNetZero -> transactionId.currentTextColor
            valueText.contains("-") -> getColor(R.color.negativeTransaction)
            else -> {
                valueText = ("+$valueText")
                getColor(R.color.positiveTransaction)
            }
        })
        transactionValue.text = valueText

        itemView.setOnClickListener {
            GenUtil.launchCustomTabs(itemView.context, "https://explore.sia.tech/hashes/${transaction.transactionId}")
        }
    }

    private fun getColor(resId: Int) = ContextCompat.getColor(itemView.context, resId)

    companion object {
        private val df = SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault())
    }
}