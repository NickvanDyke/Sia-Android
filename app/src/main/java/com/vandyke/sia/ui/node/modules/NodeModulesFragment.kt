/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.node.modules

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.*
import com.vandyke.sia.R
import com.vandyke.sia.dagger.SiaViewModelFactory
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.visibleIf
import io.github.tonnyl.light.Light
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_node_modules.*
import kotlinx.android.synthetic.main.holder_module.*
import javax.inject.Inject

class NodeModulesFragment : BaseFragment() {
    override val title: String = "Node Modules"
    override val layoutResId: Int = R.layout.fragment_node_modules
    override val hasOptionsMenu: Boolean = true

    @Inject
    lateinit var factory: SiaViewModelFactory
    private lateinit var vm: NodeModulesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context!!.getAppComponent().inject(this)

        val adapter = ModulesAdapter()
        modules_list.adapter = adapter

        vm = ViewModelProviders.of(this, factory).get(NodeModulesViewModel::class.java)

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

    private fun showDeleteConfirmationDialog(module: Module, internal: Boolean) {
        AlertDialog.Builder(context!!)
                .setTitle("Confirm")
                .setMessage(
                        "Are you sure you want to delete all ${module.text} files from ${if (internal) "internal" else "external"} storage?"
                                + when (module) {
                            Module.WALLET -> "Ensure your wallet seed is recorded elsewhere first."
                            Module.CONSENSUS -> "You'll have to re-sync the blockchain."
                            else -> ""
                        })
                .setPositiveButton("Yes") { _, _ -> vm.deleteModule(module, internal) }
                .setNegativeButton("No", null)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_node_modules, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.modules_info) {
            AlertDialog.Builder(context!!)
                    .setTitle("Modules info")
                    .setMessage("The switch enables/disables the module on the Sia node. Internal/External storage indicates " +
                            "how much storage space that module is using on your device - tap to delete the files from that module. " +
                            "The Sia node will automatically restart after deleting module files.")
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
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
                    Prefs.modulesString = Prefs.modulesString.replace(module.name[0].toString(), "", true)
                } else if (!Prefs.modulesString.contains(module.name[0], true)) {
                    Prefs.modulesString += module.name[0].toLowerCase()
                }
            }

            // so turns out that you can delete node module folders while it's running, but it
            // won't take effect until after restarting the node. Observe the folders from
            // SiadSource and watch for delete event? Or just call restart on it in the vm?
            module_internal_layout.setOnClickListener {
                showDeleteConfirmationDialog(module, true)
            }

            module_external_layout.setOnClickListener {
                showDeleteConfirmationDialog(module, false)
            }
        }

        fun bind(module: ModuleData) {
            this.module = module.type
            module_name.text = module.type.text
            module_switch.isChecked = module.on

            module_internal_size.text = StorageUtil.readableFilesizeString(module.internalSize)
            module_internal_layout.visibleIf(module.internalSize > 0)

            module_external_size.text = StorageUtil.readableFilesizeString(module.externalSize)
            module_external_layout.visibleIf(module.externalSize > 0)
        }
    }
}

