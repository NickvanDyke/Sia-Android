/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.view.list

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import vandyke.siamobile.R

class FileHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layout: ConstraintLayout = itemView.findViewById(R.id.fileLayout)
    val image: ImageView = itemView.findViewById(R.id.fileImage)
    val name: TextView = itemView.findViewById(R.id.fileName)
    val size: TextView = itemView.findViewById(R.id.fileSize)
    val more: ImageButton = itemView.findViewById(R.id.fileMore)
}