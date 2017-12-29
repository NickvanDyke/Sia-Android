/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.ui.renter.files.view.list

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.vandyke.siamobile.R
import com.vandyke.siamobile.data.local.data.renter.Dir
import com.vandyke.siamobile.ui.renter.files.viewmodel.FilesViewModel

class DirHolder(itemView: View) : NodeHolder(itemView) {
    val image: ImageView = itemView.findViewById(R.id.dirImage)
    val name: TextView = itemView.findViewById(R.id.dirName)
    val size: TextView = itemView.findViewById(R.id.dirSize)
    val more: ImageButton = itemView.findViewById(R.id.dirMore)

    fun bind(dir: Dir, viewModel: FilesViewModel) {
        name.text = dir.name
//        size.text = GenUtil.readableFilesizeString(dir.size)
        itemView.setOnClickListener { v -> viewModel.changeDir(dir.path) }
        more.setOnClickListener {
            viewModel.displayDetails(dir)
        }
    }
}