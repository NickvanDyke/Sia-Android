/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.node.modules

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vandyke.sia.R
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.rx.observe
import kotlinx.android.synthetic.main.fragment_node_modules.*

class NodeModulesFragment : BaseFragment() {
    override val title: String = "Node Modules"
    override val layoutResId: Int = R.layout.fragment_node_modules

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ModulesAdapter()
        modules_list.adapter = adapter

        val vm = ViewModelProviders.of(this).get(NodeModulesViewModel::class.java)

        vm.modules.observe(this, adapter::submitList)
    }
}

class ModulesAdapter : ListAdapter<ModuleData, ModuleHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleHolder {
        return ModuleHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_module, parent, false))
    }

    override fun onBindViewHolder(holder: ModuleHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ModuleData>() {
            override fun areItemsTheSame(oldItem: ModuleData, newItem: ModuleData): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun areContentsTheSame(oldItem: ModuleData, newItem: ModuleData): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }
}

class ModuleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(module: ModuleData) {

    }
}