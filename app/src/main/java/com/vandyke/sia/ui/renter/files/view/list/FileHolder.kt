/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.showDialogAndKeyboard

class FileHolder(itemView: View, val viewModel: FilesViewModel) : NodeHolder(itemView) {
    private val image: ImageView = itemView.findViewById(R.id.fileImage)
    private val name: TextView = itemView.findViewById(R.id.fileName)
    private val size: TextView = itemView.findViewById(R.id.fileSize)
    private val more: ImageButton = itemView.findViewById(R.id.fileMore)

    private val moreMenu = PopupMenu(itemView.context, more)

    private lateinit var file: RenterFileData

    init {
        itemView.setOnClickListener(null)
        more.setOnClickListener {
            moreMenu.show()
        }

        moreMenu.inflate(R.menu.file_menu)
        moreMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.fileRename -> {
                    val dialogView = View.inflate(itemView.context, R.layout.edit_text_field, null)
                    AlertDialog.Builder(itemView.context)
                            .setTitle("Rename ${file.name}")
                            .setView(dialogView)
                            .setPositiveButton("Rename", { _, _ ->
                                viewModel.renameFile(file, dialogView.findViewById<EditText>(R.id.field).text.toString())
                            })
                            .setNegativeButton("Cancel", null)
                            .showDialogAndKeyboard()
                }
                R.id.fileDelete -> viewModel.deleteFile(file)
            }
            true
        }
    }

    fun bind(file: RenterFileData) {
        this.file = file
        name.text = file.name
        size.text = GenUtil.readableFilesizeString(file.filesize)
    }
}