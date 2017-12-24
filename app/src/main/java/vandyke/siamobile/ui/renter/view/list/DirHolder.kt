/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.renter.view.list

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import vandyke.siamobile.R
import vandyke.siamobile.data.local.Dir
import vandyke.siamobile.ui.renter.viewmodel.RenterViewModel

class DirHolder(itemView: View) : NodeHolder(itemView) {
    val image: ImageView = itemView.findViewById(R.id.dirImage)
    val name: TextView = itemView.findViewById(R.id.dirName)
    val size: TextView = itemView.findViewById(R.id.dirSize)
    val more: ImageButton = itemView.findViewById(R.id.dirMore)

    fun bind(dir: Dir, viewModel: RenterViewModel) {
        name.text = dir.name
//        size.text = GenUtil.readableFilesizeString(dir.size)
        itemView.setOnClickListener { v -> viewModel.changeDir(dir.path) }
        more.setOnClickListener {
            viewModel.displayDetails(dir)
        }
    }
}