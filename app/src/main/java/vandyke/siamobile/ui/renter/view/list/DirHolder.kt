/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.view.list

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import vandyke.siamobile.R
import vandyke.siamobile.backend.data.renter.SiaDir
import vandyke.siamobile.ui.renter.viewmodel.RenterViewModel
import vandyke.siamobile.util.GenUtil

class DirHolder(itemView: View) : NodeHolder(itemView) {
    val image: ImageView = itemView.findViewById(R.id.dirImage)
    val name: TextView = itemView.findViewById(R.id.dirName)
    val size: TextView = itemView.findViewById(R.id.dirSize)

    fun bind(node: SiaDir, viewModel: RenterViewModel) {
        name.text = node.name
        size.text = GenUtil.readableFilesizeString(node.size)
        itemView.setOnClickListener { v -> viewModel.currentDir.value = node }
    }
}