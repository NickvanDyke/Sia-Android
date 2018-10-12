/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.common

import android.view.View
import android.widget.TextView
import com.vandyke.sia.R
import com.vandyke.sia.util.KeyboardUtil
import io.github.tonnyl.light.Light

class TextHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    val text: TextView = itemView.findViewById(R.id.textCopyView)

    init {
        itemView.setOnClickListener {
            KeyboardUtil.copyToClipboard(text.context, text.text)
            Light.info(itemView, "Copied selection to clipboard", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }
    }
}