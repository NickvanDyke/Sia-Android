/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.view.list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import vandyke.siamobile.R
import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.data.data.renter.SiaFile
import vandyke.siamobile.data.data.renter.SiaNode
import vandyke.siamobile.ui.renter.viewmodel.RenterViewModel

class RenterAdapter(private val viewModel: RenterViewModel) : RecyclerView.Adapter<NodeHolder>() {

    private val DIR = 0
    private val FILE = 1

    private var nodes = listOf<SiaNode>()

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
            holder.bind(nodes[position] as SiaDir, viewModel)
        else if (holder is FileHolder)
            holder.bind(nodes[position] as SiaFile, viewModel)
    }

    override fun getItemViewType(position: Int): Int {
        return if (nodes[position] is SiaDir) DIR else FILE
    }

    override fun getItemCount(): Int {
        return nodes.size
    }

    fun displayDir(dir: SiaDir) {
        nodes = dir.nodes
        notifyDataSetChanged()
    }
}