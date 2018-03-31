/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.view.View
import com.vandyke.sia.data.models.renter.Dir
import com.vandyke.sia.ui.renter.files.view.FilesFragment

class DirHolder(itemView: View, filesFragment: FilesFragment) : NodeHolder(itemView, filesFragment) {
    private lateinit var dir: Dir

    fun bind(dir: Dir) {
        super.bind(dir)
        this.dir = dir
    }
}