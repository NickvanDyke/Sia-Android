/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.transactionslist

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.vandyke.sia.R
import com.vandyke.sia.data.models.wallet.TransactionData
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.round
import com.vandyke.sia.util.toSC
import java.text.SimpleDateFormat
import java.util.*

class TransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val transactionStatus: TextView = itemView.findViewById(R.id.transactionStatus)
    private val transactionId: TextView = itemView.findViewById(R.id.transactionId)
    private val transactionValue: TextView = itemView.findViewById(R.id.transactionValue)

    fun bind(transaction: TransactionData) {
        val timeString: String
        if (!transaction.confirmed) {
            timeString = "Unconfirmed"
            transactionStatus.setTextColor(Color.RED)
        } else {
            timeString = df.format(transaction.confirmationDate)
            transactionStatus.setTextColor(transactionId.currentTextColor)
        }
        transactionStatus.text = timeString

        transactionId.text = transaction.transactionId

        var valueText = transaction.netValue.toSC().round().toPlainString()
        transactionValue.setTextColor(when {
            transaction.isNetZero -> transactionId.currentTextColor
            valueText.contains("-") -> red
            else -> {
                valueText = ("+$valueText")
                green
            }
        })
        transactionValue.text = valueText

        itemView.setOnClickListener {
            GenUtil.launchCustomTabs(itemView.context, "https://explore.sia.tech/explorer/hashes/${transaction.transactionId}")
        }
    }

    companion object {
        private val df = SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault())
        private val red = Color.rgb(186, 63, 63) // TODO: choose better colors maybe
        private val green = Color.rgb(0, 114, 11)
    }
}