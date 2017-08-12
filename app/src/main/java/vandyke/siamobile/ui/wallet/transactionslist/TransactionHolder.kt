/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.transactionslist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import vandyke.siamobile.R

class TransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val transactionStatus: TextView = itemView.findViewById(R.id.transactionStatus)
    val transactionId: TextView = itemView.findViewById(R.id.transactionHeaderId)
    val transactionValue: TextView = itemView.findViewById(R.id.transactionValue)
}