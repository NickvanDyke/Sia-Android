/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.view.View
import com.vandyke.sia.data.models.renter.Dir
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel

class DirHolder(itemView: View, viewModel: FilesViewModel) : NodeHolder(itemView, viewModel) {
    private lateinit var dir: Dir

    init {
        itemView.setOnClickListener {
            viewModel.changeDir(dir.path)
        }
    }

    fun bind(dir: Dir) {
        super.bind(dir)
        this.dir = dir
    }
}