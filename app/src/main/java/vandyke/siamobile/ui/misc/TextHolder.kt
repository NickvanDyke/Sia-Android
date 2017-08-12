/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.misc

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import vandyke.siamobile.R

class TextHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text: TextView = itemView.findViewById(R.id.textCopyView)
}