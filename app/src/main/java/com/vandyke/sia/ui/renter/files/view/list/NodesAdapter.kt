/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.vandyke.sia.R
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.local.models.renter.Node
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel

class NodesAdapter(val viewModel: FilesViewModel) : RecyclerView.Adapter<NodeHolder>() {

    private val DIR = 0
    private val FILE = 1

    private val nodes = mutableListOf<Node>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeHolder {
        if (viewType == DIR) {
            val holder = DirHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_renter_dir, parent, false))
            return holder
        } else {
            val holder = FileHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_renter_file, parent, false))
            return holder
        }
    }

    override fun onBindViewHolder(holder: NodeHolder, position: Int) {
        if (holder is DirHolder)
            holder.bind(nodes[position] as Dir, viewModel)
        else if (holder is FileHolder)
            holder.bind(nodes[position] as RenterFileData, viewModel)
    }

    override fun getItemViewType(position: Int) = if (nodes[position] is Dir) DIR else FILE

    override fun getItemCount() = nodes.size

    fun display(newNodes: List<Node>) {
        val diffResult = DiffUtil.calculateDiff(NodesDiffUtil(this.nodes, newNodes))
        nodes.clear()
        nodes.addAll(newNodes)
        diffResult.dispatchUpdatesTo(this) // TODO: causes delay when the entire list changes, as opposed to notifyDataSetChanged
    }

    inner class NodesDiffUtil(private val oldList: List<Node>, private val newList: List<Node>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldList[oldItemPosition].path == newList[newItemPosition].path

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition].name == newList[newItemPosition].name
                        && oldList[oldItemPosition].modified == newList[newItemPosition].modified
    }
}