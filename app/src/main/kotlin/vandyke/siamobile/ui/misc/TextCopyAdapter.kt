package vandyke.siamobile.ui.misc

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import vandyke.siamobile.R

class TextCopyAdapter(val data: ArrayList<String>) : RecyclerView.Adapter<TextHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TextHolder {
        return TextHolder(LayoutInflater.from(parent?.context).inflate(R.layout.list_item_text_copy, parent, false))
    }

    override fun onBindViewHolder(holder: TextHolder?, position: Int) {
        println(holder?.text)
        holder?.text?.text = data[position]
    }

    override fun getItemCount(): Int {
        println(data.size)
        return data.size
    }
}