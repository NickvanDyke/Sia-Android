/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.arch.lifecycle.Observer
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.vandyke.sia.R
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.GenUtil

class DirHolder(itemView: View, val viewModel: FilesViewModel) : NodeHolder(itemView) {
    private val image: ImageView = itemView.findViewById(R.id.dirImage)
    private val name: TextView = itemView.findViewById(R.id.dirName)
    private val size: TextView = itemView.findViewById(R.id.dirSize)

    private lateinit var dir: Dir

    private val obs = Observer<List<Node>> {
        if (it?.find { it.path == dir.path} != null) {
            itemView.setBackgroundColor(selectedBg)
            itemView.background.alpha = selectedAlpha
        } else {
            itemView.setBackgroundColor(normalBg)
            itemView.background.alpha = 255
        }
    }

    init {
        itemView.setOnClickListener {
            viewModel.changeDir(dir.path)
        }

        image.setOnClickListener {
            viewModel.toggleSelect(dir)
        }
    }

    fun bind(dir: Dir) {
        this.dir = dir
        viewModel.selectedNodes.removeObserver(obs)
        viewModel.selectedNodes.observeForever(obs)
        name.text = dir.name
        size.text = GenUtil.readableFilesizeString(dir.size)
    }
}