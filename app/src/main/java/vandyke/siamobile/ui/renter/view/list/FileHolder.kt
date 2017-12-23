/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.view.list

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import vandyke.siamobile.R
import vandyke.siamobile.data.local.File
import vandyke.siamobile.ui.renter.viewmodel.RenterViewModel

class FileHolder(itemView: View) : NodeHolder(itemView) {
    val image: ImageView = itemView.findViewById(R.id.fileImage)
    val name: TextView = itemView.findViewById(R.id.fileName)
    val size: TextView = itemView.findViewById(R.id.fileSize)
    val more: ImageButton = itemView.findViewById(R.id.fileMore)

    fun bind(file: File, viewModel: RenterViewModel) {
        name.text = file.name
//        size.text = GenUtil.readableFilesizeString(file.size)
        itemView.setOnClickListener(null)
        more.setOnClickListener {
            viewModel.displayDetails(file)
        }
    }
}