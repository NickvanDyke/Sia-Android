/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.node.modules

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.StorageUtil
import io.github.tonnyl.light.Light
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_node_modules.*
import kotlinx.android.synthetic.main.holder_module.*

class NodeModulesFragment : BaseFragment() {
    override val title: String = "Node Modules"
    override val layoutResId: Int = R.layout.fragment_node_modules

    private lateinit var vm: NodeModulesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ModulesAdapter()
        modules_list.adapter = adapter

        vm = ViewModelProviders.of(this).get(NodeModulesViewModel::class.java)

        vm.modules.observe(this, adapter::submitList)

        vm.success.observe(this) {
            Light.success(view, it, Snackbar.LENGTH_LONG).show()
        }

        vm.error.observe(this) {
            Light.error(view, it, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onShow() {
        super.onShow()
        vm.onShow()
    }

    override fun onHide() {
        super.onHide()
        vm.onHide()
    }

    inner class ModulesAdapter : RecyclerView.Adapter<ModuleHolder>() {
        private var list = listOf<ModuleData>()
        private var loadedModules = false
        private val holders = mutableListOf<ModuleHolder>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleHolder {
            val holder = ModuleHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_module, parent, false))
            holders.add(holder)
            return holder
        }

        override fun onBindViewHolder(holder: ModuleHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size

        /* We have our own implementation because using ListAdapter and DiffUtil would cause the
         * view holder to flash every time it updated, which is very often in this case */
        fun submitList(newList: List<ModuleData>) {
            if (!loadedModules) {
                this.list = newList
                notifyDataSetChanged()
                loadedModules = true
            } else {
                for (i in 0..4)
                    if (list[i] != newList[i])
                        holders[i].bind(newList[i])
                this.list = newList
            }
        }
    }

    inner class ModuleHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {
        override val containerView: View? = itemView

        private lateinit var module: Module

        init {
            module_switch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!isChecked) {
                    Prefs.modulesString = Prefs.modulesString.replace(module.text[0].toString(), "", true)
                } else if (!Prefs.modulesString.contains(module.text[0], true)) {
                    Prefs.modulesString += module.text[0].toLowerCase()
                }
            }

            // so turns out that you can delete node module folders while it's running, but it
            // won't take effect until after restarting the node. Observe the folders from
            // SiadSource and watch for delete event? Or just call restart here?
            // TODO: show confirmation before deleting
            module_internal_layout.setOnClickListener {
                vm.deleteModule(module, true)
            }

            module_external_layout.setOnClickListener {
                vm.deleteModule(module, false)
            }
        }

        fun bind(module: ModuleData) {
            this.module = module.type
            module_name.text = module.type.text
            module_switch.isChecked = module.on

            if (module.internalSize > 0) {
                module_internal_size.text = StorageUtil.readableFilesizeString(module.internalSize)
                module_internal_size.visibility = View.VISIBLE
                internal_storage_header.visibility = View.VISIBLE
                module_internal_layout.visibility = View.VISIBLE
            } else {
                module_internal_size.visibility = View.GONE
                internal_storage_header.visibility = View.GONE
                module_internal_layout.visibility = View.GONE
            }

            if (module.externalSize > 0) {
                module_external_size.text = StorageUtil.readableFilesizeString(module.externalSize)
                module_external_size.visibility = View.VISIBLE
                external_storage_header.visibility = View.VISIBLE
                module_external_layout.visibility = View.VISIBLE
            } else {
                module_external_size.visibility = View.GONE
                external_storage_header.visibility = View.GONE
                module_external_layout.visibility = View.GONE
            }
        }
    }
}

