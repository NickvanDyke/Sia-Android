package com.vandyke.sia.ui.node.modules

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vandyke.sia.R
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.recursiveLength
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_module_storage.*
import java.io.File

class ModuleStorageAdapter : RecyclerView.Adapter<ModuleStorageHolder>() {
    var dirs: List<File> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleStorageHolder {
        return ModuleStorageHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_module_storage, parent, false))
    }

    override fun onBindViewHolder(holder: ModuleStorageHolder, position: Int) {
        holder.bind(dirs[position])
    }

    override fun getItemCount() = dirs.size
}

class ModuleStorageHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {
    override val containerView: View? = itemView

    fun bind(dir: File) {
        module_storage_header.text = dir.absolutePath
        module_storage_size.text = StorageUtil.readableFilesizeString(dir.recursiveLength())
    }
}