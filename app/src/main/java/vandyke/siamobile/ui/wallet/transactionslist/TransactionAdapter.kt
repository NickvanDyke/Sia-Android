package vandyke.siamobile.ui.wallet.transactionslist

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import vandyke.siamobile.R
import vandyke.siamobile.backend.models.wallet.TransactionModel
import vandyke.siamobile.ui.MainActivity
import vandyke.siamobile.util.round
import vandyke.siamobile.util.toSC
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TransactionAdapter : RecyclerView.Adapter<TransactionHolder>() {
    private var transactions: ArrayList<TransactionModel> = ArrayList()
    private val df = SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault())
    private val red = Color.rgb(186, 63, 63) // TODO: choose better colors maybe
    private val green = Color.rgb(0, 114, 11)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_tx, parent, false)
        val holder = TransactionHolder(view)
        view.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://explorer.siahub.info/hash/${holder.transactionId.text.toString().replace("\n", "")}"))
            context?.startActivity(intent)
        }
        return holder
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val tx = transactions[position]
        val timeString: String
        if (!tx.confirmed) {
            timeString = "Unconfirmed"
            holder.transactionStatus.setTextColor(Color.RED)
        } else {
            timeString = df.format(tx.confirmationdate)
            holder.transactionStatus.setTextColor(MainActivity.defaultTextColor)
        }
        holder.transactionStatus.text = timeString

        val id = tx.transactionid
        holder.transactionId.text = "${id.substring(0, id.length / 2)}\n${id.substring(id.length / 2)}"

        var valueText = tx.netValue.toSC().round().toPlainString()
        if (tx.isNetZero) {
            holder.transactionValue.setTextColor(MainActivity.defaultTextColor)
        } else if (valueText.contains("-")) {
            holder.transactionValue.setTextColor(red)
        } else {
            valueText = "+" + valueText
            holder.transactionValue.setTextColor(green)
        }
        holder.transactionValue.text = valueText
    }

    override fun getItemCount(): Int = transactions.size

    fun setTransactions(transactions: ArrayList<TransactionModel>) { this.transactions = transactions }
}