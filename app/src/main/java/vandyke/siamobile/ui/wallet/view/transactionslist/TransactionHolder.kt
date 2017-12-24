/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.wallet.view.transactionslist

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import vandyke.siamobile.R
import vandyke.siamobile.data.data.wallet.TransactionData
import vandyke.siamobile.util.round
import vandyke.siamobile.util.toSC
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
            timeString = df.format(transaction.confirmationdate)
            transactionStatus.setTextColor(transactionId.currentTextColor)
        }
        transactionStatus.text = timeString

        val id = transaction.transactionid
        transactionId.text = "${id.substring(0, id.length / 2)}\n${id.substring(id.length / 2)}"

        var valueText = transaction.netValue.toSC().round().toPlainString()
        if (transaction.isNetZero) {
            transactionValue.setTextColor(transactionId.currentTextColor)
        } else if (valueText.contains("-")) {
            transactionValue.setTextColor(red)
        } else {
            valueText = "+" + valueText
            transactionValue.setTextColor(green)
        }
        transactionValue.text = valueText
    }
    
    companion object {
        private val df = SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault())
        private val red = Color.rgb(186, 63, 63) // TODO: choose better colors maybe
        private val green = Color.rgb(0, 114, 11)
    }
}