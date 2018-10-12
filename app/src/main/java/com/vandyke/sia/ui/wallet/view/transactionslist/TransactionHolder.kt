/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.transactionslist

import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.data.models.wallet.TransactionData
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.format
import com.vandyke.sia.util.getColorRes
import com.vandyke.sia.util.toSC
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_transaction.*
import java.text.SimpleDateFormat
import java.util.*

class TransactionHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), LayoutContainer {

    override val containerView: View?
        get() = itemView

    lateinit var transaction: TransactionData

    init {
        itemView.setOnClickListener {
            GenUtil.launchCustomTabs(itemView.context, "https://explore.sia.tech/hashes/${transaction.transactionid}")
        }
    }

    fun bind(transaction: TransactionData) {
        this.transaction = transaction

        if (!transaction.confirmed) {
            transactionStatus.text = "Unconfirmed"
            transactionStatus.setTextColor(itemView.context.getColorRes(R.color.negative))
        } else {
            transactionStatus.text = df.format(transaction.confirmationDate)
            transactionStatus.setTextColor(transactionId.currentTextColor)
        }

        transactionId.text = transaction.transactionid

        var valueText = transaction.netValue.toSC().format()
        transactionValue.setTextColor(when {
            transaction.isNetZero -> transactionId.currentTextColor
            valueText.contains("-") -> itemView.context.getColorRes(R.color.negative)
            else -> {
                valueText = ("+$valueText")
                itemView.context.getColorRes(R.color.positiveTransaction)
            }
        })
        transactionValue.text = valueText
    }

    companion object {
        private val df = SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault())
    }
}