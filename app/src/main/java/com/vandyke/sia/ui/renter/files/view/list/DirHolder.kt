/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import com.vandyke.sia.R
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.GenUtil

class DirHolder(itemView: View, val viewModel: FilesViewModel) : NodeHolder(itemView) {
    private val image: ImageView = itemView.findViewById(R.id.dirImage)
    private val name: TextView = itemView.findViewById(R.id.dirName)
    private val size: TextView = itemView.findViewById(R.id.dirSize)
    private val more: ImageButton = itemView.findViewById(R.id.dirMore)

    lateinit var dir: Dir

    private val moreMenu = PopupMenu(itemView.context, more)

    init {
        itemView.setOnClickListener { v -> viewModel.changeDir(dir.path) }

        more.setOnClickListener {
            moreMenu.show()
        }

        moreMenu.inflate(R.menu.dir_menu)
        moreMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.dirDelete -> viewModel.deleteDir(dir)
                R.id.dirRename -> {
                    val dialogView = View.inflate(itemView.context, R.layout.fragment_renter_add_dir, null)
                    AlertDialog.Builder(itemView.context)
                            .setTitle("Rename ${dir.name}")
                            .setView(dialogView)
                            .setPositiveButton("Rename", { dialogInterface, i ->
                                viewModel.renameDir(dir, dialogView.findViewById<EditText>(R.id.newDirName).text.toString())
                            })
                            .setNegativeButton("Cancel", null)
                            .show()
                }
                R.id.dirMove -> TODO()
            }
            true
        }
    }

    fun bind(dir: Dir) {
        this.dir = dir
        name.text = dir.name
        size.text = GenUtil.readableFilesizeString(dir.size)
    }
}