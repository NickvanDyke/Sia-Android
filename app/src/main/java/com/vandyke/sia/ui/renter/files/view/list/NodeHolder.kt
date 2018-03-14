/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.support.v7.widget.RecyclerView
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.util.getAttrColor


abstract class NodeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val normalBg = itemView.context.getAttrColor(android.R.attr.selectableItemBackground)
    val selectedBg = itemView.context.getAttrColor(R.attr.colorPrimaryDark)
    val selectedAlpha = 50
}
