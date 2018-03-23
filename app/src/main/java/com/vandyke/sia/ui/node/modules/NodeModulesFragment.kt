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
import io.github.tonnyl.light.Light
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_node_modules.*
import kotlinx.android.synthetic.main.holder_module.*
import java.io.File
import javax.inject.Inject

// TODO: the View portion of the modules page could be made less messy
class NodeModulesFragment : BaseFragment() {
    override val title: String = "Node Modules"
    override val layoutResId: Int = R.layout.fragment_node_modules
    override val hasOptionsMenu: Boolean = true

    @Inject
    lateinit var factory: SiaViewModelFactory
    private lateinit var vm: NodeModulesViewModel

    private val adapter = ModulesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context!!.getAppComponent().inject(this)

        modules_list.adapter = adapter

        vm = ViewModelProviders.of(this, factory).get(NodeModulesViewModel::class.java)

        vm.moduleUpdated.observe(this, adapter::notifyUpdate)

        vm.success.observe(this) {
            Light.success(view, it, Snackbar.LENGTH_LONG).show()
        }

        vm.error.observe(this) {
            Light.error(view, it, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onShow() {
        /* send an update notification for each module, so that the module's files' size will be
         * accurately reflected upon showing the modules page without needing the FileObservers
         * to fire an event first */
        Module.values().forEach { adapter.notifyUpdate(it) }
        vm.onShow()
    }

    override fun onHide() {
        vm.onHide()
    }

    fun showDeleteConfirmationDialog(module: Module, dir: File) {
        AlertDialog.Builder(context!!)
                .setMessage(
                        "Are you sure you want to delete all ${module.text} files at ${dir.absolutePath}?"
                                + when (module) {
                            Module.WALLET -> " Ensure your wallet seed is recorded elsewhere first."
                            Module.CONSENSUS -> " You'll have to re-sync the blockchain."
                            else -> ""
                        })
                .setPositiveButton("Yes") { _, _ -> vm.deleteDir(module, dir) }
                .setNegativeButton("No", null)
                .show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_node_modules, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.modules_info) {
            AlertDialog.Builder(context!!)
                    .setTitle("Modules info")
                    .setMessage("The switch enables/disables the module on the Sia node. Each module's storage usage is also shown - tap to delete.")
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    inner class ModulesAdapter : RecyclerView.Adapter<ModuleHolder>() {
        private val holders = mutableListOf<ModuleHolder>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleHolder {
            val holder = ModuleHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_module, parent, false))
            holders.add(holder)
            return holder
        }

        override fun onBindViewHolder(holder: ModuleHolder, position: Int) {
            holder.bind(vm.modules[position])
        }

        override fun getItemCount() = vm.modules.size

        fun notifyUpdate(module: Module) {
            val data = vm.modules.find { it.type == module }
            val holder = holders.find { it.module == module }
            holder?.bind(data!!)
        }
    }

    inner class ModuleHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {
        override val containerView: View? = itemView

        lateinit var module: Module
        private var storageAdapter: ModuleStorageAdapter? = null

        init {
            module_switch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!isChecked) {
                    Prefs.modulesString = Prefs.modulesString.replace(module.name[0].toString(), "", true)
                } else if (!Prefs.modulesString.contains(module.name[0], true)) {
                    Prefs.modulesString += module.name[0].toLowerCase()
                }
            }

            module_switch.setOnClickListener {
                Light.success(view!!, "${module.text} module ${if (module_switch.isChecked) "enabled" else "disabled"}, restarting Sia node...", Snackbar.LENGTH_LONG).show()
            }
        }

        fun bind(module: ModuleData) {
            if (storageAdapter == null) {
                storageAdapter = ModuleStorageAdapter(module, this@NodeModulesFragment)
                module_storage_list.adapter = storageAdapter
            }

            this.module = module.type
            module_name.text = module.type.text
            module_switch.isChecked = module.enabled

            storageAdapter!!.notifyUpdate()
        }
    }
}

