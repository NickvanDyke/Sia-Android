/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.SiaFile
import com.vandyke.sia.ui.renter.files.view.FilesFragment
import com.vandyke.sia.util.format
import kotlinx.android.synthetic.main.holder_renter_file_list.*
import kotlinx.android.synthetic.main.holder_renter_node.*

class FileHolder(itemView: View, filesFragment: FilesFragment) : NodeHolder(itemView, filesFragment) {

    fun bind(file: SiaFile) {
        super.bind(file)
        file_redundancy.text = file.redundancy.format() + "x"
        node_image.setImageResource(R.drawable.ic_file) // TODO: other images based on filetype
    }
}