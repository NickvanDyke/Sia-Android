package com.vandyke.sia.ui.node.modules

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vandyke.sia.R
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.recursiveLength
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_module_storage.*
import java.io.File

/* we use unusual-ish ways of updating the holders because calling the usual notifyDataSetChanged or
 * notifyItemChanged or DiffUtil etc. would cause difficulties clicking the items because of how often they'd re-bind */
class ModuleStorageAdapter(val module: ModuleData, val fragment: NodeModulesFragment) : androidx.recyclerview.widget.RecyclerView.Adapter<ModuleStorageAdapter.ModuleStorageHolder>() {
    private val holders = mutableListOf<ModuleStorageHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleStorageHolder {
        val holder = ModuleStorageHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_module_storage, parent, false))
        holders.add(holder)
        return holder
    }

    override fun onBindViewHolder(holder: ModuleStorageHolder, position: Int) {
        holder.bind(module.directories[position])
    }

    fun notifyUpdate() {
        holders.forEach { it.update() }
    }

    override fun getItemCount() = module.directories.size

    inner class ModuleStorageHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), LayoutContainer {
        override val containerView: View? = itemView
        private var dir: File? = null

        init {
            itemView.setOnClickListener {
                fragment.showDeleteConfirmationDialog(module.type, dir!!)
            }
        }

        fun bind(dir: File) {
            this.dir = dir
            module_storage_header.text = dir.absolutePath
            module_storage_size.text = StorageUtil.readableFilesizeString(dir.recursiveLength())
        }

        fun update() {
            dir?.let {
                module_storage_header.text = it.absolutePath
                module_storage_size.text = StorageUtil.readableFilesizeString(it.recursiveLength())
            }
        }
    }
}