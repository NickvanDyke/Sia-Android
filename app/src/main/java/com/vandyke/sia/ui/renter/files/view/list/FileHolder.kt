/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.GenUtil

class FileHolder(itemView: View, val viewModel: FilesViewModel) : NodeHolder(itemView) {
    private val image: ImageView = itemView.findViewById(R.id.fileImage)
    private val name: TextView = itemView.findViewById(R.id.fileName)
    private val size: TextView = itemView.findViewById(R.id.fileSize)
    private val more: ImageButton = itemView.findViewById(R.id.fileMore)

    private val moreMenu = PopupMenu(itemView.context, more)

    init {
        itemView.setOnClickListener(null)
        more.setOnClickListener {
            moreMenu.show()
        }

        moreMenu.inflate(R.menu.file_menu)
        moreMenu.setOnMenuItemClickListener {
            true
        }
    }

    fun bind(file: RenterFileData) {
        name.text = file.name
        size.text = GenUtil.readableFilesizeString(file.filesize)
    }
}