/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import com.vandyke.sia.R


abstract class NodeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val normalBg: Int
    val selectedBg = ContextCompat.getColor(itemView.context, R.color.colorPrimaryDark)
    val selectedAlpha = 50

    init {
        val typedValue = TypedValue()
        val theme = itemView.context.theme
        theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        normalBg = typedValue.data
    }
}
