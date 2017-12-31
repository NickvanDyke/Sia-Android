/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.transactionslist

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.vandyke.sia.R
import com.vandyke.sia.data.models.wallet.TransactionData
import com.vandyke.sia.util.round
import com.vandyke.sia.util.toSC
import java.text.SimpleDateFormat
import java.util.*

class TransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val transactionStatus: TextView = itemView.findViewById(R.id.transactionStatus)
    val transactionId: TextView = itemView.findViewById(R.id.transactionId)
    val transactionValue: TextView = itemView.findViewById(R.id.transactionValue)
    
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
//        transactionId.text = ("${id.substring(0, id.length / 2)}\n${id.substring(id.length / 2)}")

        var valueText = transaction.netValue.toSC().round().toPlainString()
        if (transaction.isNetZero) {
            transactionValue.setTextColor(transactionId.currentTextColor)
        } else if (valueText.contains("-")) {
            transactionValue.setTextColor(red)
        } else {
            valueText = ("+$valueText")
            transactionValue.setTextColor(green)
        }
        transactionValue.text = valueText

        itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://explorer.siahub.info/hash/${transaction.transactionId}"))
            itemView.context.startActivity(intent)
        }
    }
    
    companion object {
        private val df = SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault())
        private val red = Color.rgb(186, 63, 63) // TODO: choose better colors maybe
        private val green = Color.rgb(0, 114, 11)
    }
}