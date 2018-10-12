/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.Dir
import com.vandyke.sia.data.models.renter.Node
import com.vandyke.sia.data.models.renter.SiaFile
import com.vandyke.sia.ui.renter.files.view.FilesFragment

class NodesAdapter(private val filesFragment: FilesFragment) : ListAdapter<Node, NodeHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == DIR) {
            DirHolder(
                    inflater.inflate(
//                            if (viewModel.viewAsList.value)
                            R.layout.holder_renter_dir_list,
//                            else
//                                R.layout.holder_renter_dir_grid,
                            parent, false),
                    filesFragment)
        } else {
            FileHolder(
                    inflater.inflate(
//                            if (viewModel.viewAsList.value)
                                R.layout.holder_renter_file_list,
//                            else
//                                R.layout.holder_renter_file_grid,
                            parent, false),
                    filesFragment)
        }
    }

    override fun onBindViewHolder(holder: NodeHolder, position: Int) {
        if (holder is DirHolder)
            holder.bind(getItem(position) as Dir)
        else if (holder is FileHolder)
            holder.bind(getItem(position) as SiaFile)
    }

    override fun getItemViewType(position: Int) = if (getItem(position) is Dir) DIR else FILE

    companion object {
        private const val DIR = 0
        private const val FILE = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Node>() {
            override fun areItemsTheSame(oldItem: Node, newItem: Node): Boolean {
                return oldItem.path == newItem.path
            }

            override fun areContentsTheSame(oldItem: Node, newItem: Node): Boolean {
                return when {
                    oldItem is Dir && newItem is Dir -> oldItem.name == newItem.name && oldItem.size == newItem.size
                    oldItem is SiaFile && newItem is SiaFile -> oldItem.name == newItem.name && oldItem.size == newItem.size
                            && oldItem.redundancy == newItem.redundancy
                            && oldItem.uploadprogress == newItem.uploadprogress
                    else -> false
                }
            }
        }
    }
}