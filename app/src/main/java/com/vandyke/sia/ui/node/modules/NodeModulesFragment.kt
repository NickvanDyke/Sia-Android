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
import com.vandyke.sia.data.siad.SiadStatus
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.addIfNotPresent
import com.vandyke.sia.util.remove
import io.github.tonnyl.light.Light
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_node_modules.*
import kotlinx.android.synthetic.main.holder_module.*
import java.io.File
import javax.inject.Inject

// TODO: the View portion of the modules page could be made less messy
// should definitely split things into their own files. Way too many inner classes going on.
// The scrolling is also often strange. Maybe due to nester RVs
class NodeModulesFragment : BaseFragment() {
    override val title: String = "Node Modules"
    override val layoutResId: Int = R.layout.fragment_node_modules
    override val hasOptionsMenu: Boolean = true

    @Inject
    lateinit var factory: SiaViewModelFactory
    private lateinit var vm: NodeModulesViewModel

    @Inject
    lateinit var siadStatus: SiadStatus

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
                        "Delete all ${module.text} files at ${dir.absolutePath}?"
                                + when (module) {
                            Module.WALLET -> " Ensure your wallet seed is recorded elsewhere first."
                            Module.CONSENSUS -> " If they're the files Sia is currently using, you'll have to re-sync the blockchain."
                            Module.RENTER -> " If they're the files Sia is currently using, you'll lose all your contracts," +
                                    " and therefore access to all your files on the Sia network."
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
                    .setMessage("Each module on the list depends on the modules above it. The switch enables/disables the given module on the Sia node. Each module's storage usage is also shown - tap to delete it.")
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

        /* note that we update items in a sort of custom way, because using DiffUtil or calling notifyItem* on the adapter
         * causes very frequent rebinding, since the data is updating so often. This rebinding makes it extremely
         * difficult to click the items */
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
            module_switch.setOnCheckedChangeListener { _, isChecked ->
                if (!isChecked) {
                    Prefs.modulesString = Prefs.modulesString.remove(module.name[0].toString(), true)
                } else {
                    Prefs.modulesString = Prefs.modulesString.addIfNotPresent(module.name[0].toLowerCase().toString(), true)
                }
            }

            module_switch.setOnClickListener {
                Light.success(view!!,
                        "${module.text} module ${if (module_switch.isChecked) "enabled" else "disabled"}" +
                                if (siadStatus.state.value!!.processIsRunning) ", restarting Sia node..." else "",
                        Snackbar.LENGTH_LONG).show()
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

