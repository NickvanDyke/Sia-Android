package vandyke.siamobile.ui.misc

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import vandyke.siamobile.R

class TextHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text: TextView = itemView.findViewById(R.id.textCopyView)
}