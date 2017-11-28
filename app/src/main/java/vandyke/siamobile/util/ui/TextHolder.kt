/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.util.ui

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import vandyke.siamobile.R
import vandyke.siamobile.util.GenUtil
import vandyke.siamobile.util.SnackbarUtil

class TextHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text: TextView = itemView.findViewById(R.id.textCopyView)

    init {
        itemView.setOnClickListener {
            GenUtil.copyToClipboard(text.context, text.text)
            SnackbarUtil.snackbar(text, "Copied selection to clipboard", Snackbar.LENGTH_SHORT)
        }
    }
}