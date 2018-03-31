/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.animation.ValueAnimator
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.widget.RecyclerView
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.models.renter.Dir
import com.vandyke.sia.data.models.renter.Node
import com.vandyke.sia.ui.renter.files.view.FilesFragment
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.getAttrColor
import com.vandyke.sia.util.getColorRes
import com.vandyke.sia.util.rx.observe
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_renter_node.*


abstract class NodeHolder(itemView: View, filesFragment: FilesFragment) : RecyclerView.ViewHolder(itemView), LayoutContainer {
    override val containerView: View? = itemView
    protected val vm = ViewModelProviders.of(filesFragment).get(FilesViewModel::class.java)

    private val normalColor = itemView.context.getAttrColor(android.R.attr.selectableItemBackground)
    private val selectedColor = itemView.context.getColorRes(if (Prefs.oldSiaColors) R.color.selectedBgOld else R.color.selectedBg)

    private val vmSelected
        get() = vm.selectedNodes.value.any { it.path == node.path }

    private var selected = false
    private var imageTouched = false

    private var selectAnimator: ValueAnimator? = null
    private var deselectAnimator: ValueAnimator? = null
    private var currentColor: Int = normalColor

    private lateinit var node: Node

    init {
        /* we pass through touch events on the image so that we can still get the ripple effect */
        node_image.setOnTouchListener { v, event ->
            imageTouched = true
            false
        }

        itemView.setOnClickListener {
            if (imageTouched) {
                vm.toggleSelect(node)
                imageTouched = false
            } else if (node is Dir) {
                vm.changeDir(node.path)
            }
        }

        itemView.setOnLongClickListener {
            imageTouched = false
            select(false)
            vm.toggleSelect(node)
            true
        }

        vm.selectedNodes.observe(filesFragment) {
            if (!::node.isInitialized)
                return@observe
            if (it.any { it.path == node.path })
                select(true)
            else
                deselect(true)
        }
    }

    protected fun bind(node: Node) {
        this.node = node
        node_name.text = node.name
        node_size.text = StorageUtil.readableFilesizeString(node.size)
        if (vmSelected)
            select(false)
        else
            deselect(false)
    }

    private fun select(animate: Boolean) {
        if (selected)
            return
        deselectAnimator?.cancel()
        if (animate) {
            selectAnimator = ValueAnimator.ofArgb(currentColor, selectedColor).apply {
                duration = 150
                addUpdateListener { setBackgroundColor(it.animatedValue as Int) }
                start()
            }
        } else {
            setBackgroundColor(selectedColor)
        }
        selected = true
    }

    private fun deselect(animate: Boolean) {
        if (!selected)
            return
        selectAnimator?.cancel()
        if (animate) {
            deselectAnimator = ValueAnimator.ofArgb(currentColor, normalColor).apply {
                duration = 1000
                addUpdateListener { setBackgroundColor(it.animatedValue as Int) }
                start()
            }
        } else {
            setBackgroundColor(normalColor)
        }
        selected = false
    }

    private fun setBackgroundColor(color: Int) {
        itemView.setBackgroundColor(color)
        currentColor = color
    }
}
