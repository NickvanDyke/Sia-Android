/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.files

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import vandyke.siamobile.R
import vandyke.siamobile.backend.renter.SiaDir
import vandyke.siamobile.ui.renter.RenterFragment
import vandyke.siamobile.util.GenUtil

class FilesAdapter(private val renterFragment: RenterFragment) : RecyclerView.Adapter<FileHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_file, parent, false)
        return FileHolder(view)
    }

    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        val node = renterFragment.currentDir.nodes[position]
        holder.name.text = node.name
        holder.size.text = GenUtil.readableFilesizeString(node.size)
        if (node is SiaDir) {
            holder.layout.setOnClickListener({ v -> renterFragment.currentDir = node })
            holder.image.setImageResource(R.drawable.ic_folder)
        } else {
            holder.layout.setOnClickListener(null)
            holder.image.setImageResource(R.drawable.ic_file)
        }
    }

    override fun getItemCount(): Int {
        return renterFragment.currentDir.nodes.size
    }
}