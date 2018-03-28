/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.support.v7.widget.RecyclerView
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.Node
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.getAttrColor
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_renter_node.*


abstract class NodeHolder(itemView: View, val viewModel: FilesViewModel) : RecyclerView.ViewHolder(itemView), LayoutContainer {
    override val containerView: View? = itemView

    val normalBg = itemView.context.getAttrColor(android.R.attr.selectableItemBackground)
    val selectedBg = itemView.context.getAttrColor(R.attr.colorPrimaryDark)
    val selectedAlpha = 50

    private lateinit var node: Node

    init {
        node_image.setOnClickListener {
            viewModel.toggleSelect(node)
        }

        // TODO: use a solution other than observeForever()
        viewModel.selectedNodes.observeForevs {
            if (!::node.isInitialized)
                return@observeForevs
            determineSelected(it)
        }
    }

    protected fun bind(node: Node) {
        this.node = node
        determineSelected(viewModel.selectedNodes.value)
        node_name.text = node.name
        node_size.text = StorageUtil.readableFilesizeString(node.size)
    }

    private fun determineSelected(nodes: List<Node>) {
        if (nodes.find { it.path == node.path } != null)
            select()
        else
            deselect()
    }

    private fun select() {
        itemView.setBackgroundColor(selectedBg)
        itemView.background.alpha = selectedAlpha
    }

    private fun deselect() {
        itemView.setBackgroundColor(normalBg)
        itemView.background.alpha = 255
    }
}
