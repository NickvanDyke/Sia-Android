/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.os.Build
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.SiaFile
import com.vandyke.sia.ui.renter.files.view.FilesFragment
import com.vandyke.sia.util.*
import kotlinx.android.synthetic.main.holder_renter_file_list.*
import kotlinx.android.synthetic.main.holder_renter_node.*
import net.cachapa.expandablelayout.ExpandableLayout

class FileHolder(itemView: View, filesFragment: FilesFragment) : NodeHolder(itemView, filesFragment) {
    val file
        get() = node as SiaFile

    private var updatedExpandable = false

    init {
        itemView.setOnClickListener {
            if (!baseItemViewOnClick())
                file_expandable.toggle()
        }

        file_expandable.setOnExpansionUpdateListener { expansionFraction, state ->
            if (!updatedExpandable && state == ExpandableLayout.State.EXPANDING) {
                updatedExpandable = true
                file_uploadedbytes.text = StorageUtil.readableFilesizeString(file.uploadedbytes)
                file_expiration.text = "Block ${file.expiration.format()} (~${SiaUtil.blockHeightToReadableTimeDiff(file.expiration)})"
                file_renewing.text = if (file.renewing) "Yes" else "No"
                file_localpath.text = if (file.localpath.isNotEmpty()) file.localpath else "File not present locally"
            }
        }
    }

    fun bind(file: SiaFile) {
        super.bind(file)
        // could keep track in the viewmodel of what nodes are expanded, similar to selected, instead of just always collapsing
        file_expandable.collapse(false)
        updatedExpandable = false

        file_redundancy.text = file.redundancy.format() + "x"

        val progress = file.uploadprogress.toInt()
        if (progress == 100) {
            file_upload_progressbar.gone()
            file_upload_progress_text.gone()
        } else {
            when {
                file.available -> file_upload_progressbar.setProgressColorAttrRes(R.attr.colorPrimary)
                else -> file_upload_progressbar.setProgressColorRes(R.color.negative)
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                file_upload_progressbar.progress = progress
            else
                file_upload_progressbar.setProgress(progress, true)
            file_upload_progress_text.text = "$progress%"
            file_upload_progressbar.visible()
            file_upload_progress_text.visible()
        }

        node_image.setImageResource(R.drawable.ic_file_white) // TODO: other images based on filetype
    }
}