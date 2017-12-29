/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.ui.common

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.vandyke.siamobile.R
import com.vandyke.siamobile.util.GenUtil
import com.vandyke.siamobile.util.SnackbarUtil

class TextHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text: TextView = itemView.findViewById(R.id.textCopyView)

    init {
        itemView.setOnClickListener {
            GenUtil.copyToClipboard(text.context, text.text)
            SnackbarUtil.snackbar(text, "Copied selection to clipboard", Snackbar.LENGTH_SHORT)
        }
    }
}