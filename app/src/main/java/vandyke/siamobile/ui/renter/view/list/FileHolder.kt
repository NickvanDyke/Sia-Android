/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.view.list

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import vandyke.siamobile.R
import vandyke.siamobile.data.data.renter.SiaFile
import vandyke.siamobile.util.GenUtil

class FileHolder(itemView: View) : NodeHolder(itemView) {
    val image: ImageView = itemView.findViewById(R.id.fileImage)
    val name: TextView = itemView.findViewById(R.id.fileName)
    val size: TextView = itemView.findViewById(R.id.fileSize)
    val more: ImageButton = itemView.findViewById(R.id.fileMore)

    fun bind(node: SiaFile) {
        name.text = node.name
        size.text = GenUtil.readableFilesizeString(node.size)
        itemView.setOnClickListener(null)
        more.setOnClickListener {
            val menu = PopupMenu(itemView.context, more)
            menu.inflate(R.menu.file_menu)
            // TODO: need to access renter's view model from here, to call delete/rename/etc methods on it
            menu.setOnMenuItemClickListener {
                when (it.itemId) {
//                        R.id.fileDownload ->
                }
            }
            menu.show()
        }
    }
}