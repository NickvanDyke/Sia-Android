/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.common

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.vandyke.sia.R
import com.vandyke.sia.util.KeyboardUtil
import com.vandyke.sia.util.SnackbarUtil

class TextHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text: TextView = itemView.findViewById(R.id.textCopyView)

    init {
        itemView.setOnClickListener {
            KeyboardUtil.copyToClipboard(text.context, text.text)
            SnackbarUtil.showSnackbar(text, "Copied selection to clipboard", Snackbar.LENGTH_SHORT)
        }
    }
}