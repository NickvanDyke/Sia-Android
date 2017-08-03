package vandyke.siamobile.ui.wallet.transactionslist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import vandyke.siamobile.R

class TransactionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var transactionStatus: TextView = itemView.findViewById(R.id.transactionStatus)
    var transactionId: TextView = itemView.findViewById(R.id.transactionHeaderId)
    var transactionValue: TextView = itemView.findViewById(R.id.transactionValue)
}