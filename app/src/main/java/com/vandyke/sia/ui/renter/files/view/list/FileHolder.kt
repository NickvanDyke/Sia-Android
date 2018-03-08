/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.arch.lifecycle.Observer
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.vandyke.sia.R
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.StorageUtil

class FileHolder(itemView: View, val viewModel: FilesViewModel) : NodeHolder(itemView) {
    private val image: ImageView = itemView.findViewById(R.id.fileImage)
    private val name: TextView = itemView.findViewById(R.id.fileName)
    private val size: TextView = itemView.findViewById(R.id.fileSize)

    private lateinit var file: RenterFileData

    private val obs = Observer<List<Node>> {
        if (it?.find { it.path == file.path} != null) {
            itemView.setBackgroundColor(selectedBg)
            itemView.background.alpha = selectedAlpha
        } else {
            itemView.setBackgroundColor(normalBg)
            itemView.background.alpha = 255
        }
    }

    init {
        image.setOnClickListener {
            viewModel.toggleSelect(file)
        }
    }

    fun bind(file: RenterFileData) {
        this.file = file
        viewModel.selectedNodes.removeObserver(obs)
        viewModel.selectedNodes.observeForever(obs)
        name.text = file.name
        size.text = StorageUtil.readableFilesizeString(file.size)
    }
}