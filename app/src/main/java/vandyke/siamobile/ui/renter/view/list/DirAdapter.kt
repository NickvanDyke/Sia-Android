/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.view.list

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import vandyke.siamobile.R
import vandyke.siamobile.backend.data.renter.SiaDir
import vandyke.siamobile.ui.renter.view.RenterFragment
import vandyke.siamobile.util.GenUtil

class DirAdapter(private val renterFragment: RenterFragment, private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val DIR = 0
    private val FILE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == DIR) {
            val holder = DirHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_dir, parent, false))
            return holder
        } else {
            val holder = FileHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_file, parent, false))
            return holder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val node = renterFragment.currentDir.nodes[position]
        if (holder is DirHolder) {
            holder.name.text = node.name
            holder.size.text = GenUtil.readableFilesizeString(node.size)
            holder.layout.setOnClickListener { v -> renterFragment.currentDir = node as SiaDir }
        } else if (holder is FileHolder) {
            holder.name.text = node.name
            holder.size.text = GenUtil.readableFilesizeString(node.size)
            holder.layout.setOnClickListener(null)
            holder.more.setOnClickListener {
                val menu = PopupMenu(context, holder.more)
                menu.inflate(R.menu.file_menu)
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
//                        R.id.fileDownload ->
                    }
                }
                menu.show()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (renterFragment.currentDir.nodes[position] is SiaDir) DIR else FILE
    }

    override fun getItemCount(): Int {
        return renterFragment.currentDir.nodes.size
    }
}