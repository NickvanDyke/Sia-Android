/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.list

import android.os.Build
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.SiaFile
import com.vandyke.sia.ui.renter.files.view.FilesFragment
import com.vandyke.sia.util.format
import com.vandyke.sia.util.gone
import com.vandyke.sia.util.setProgressColorRes
import com.vandyke.sia.util.visible
import kotlinx.android.synthetic.main.holder_renter_file_list.*
import kotlinx.android.synthetic.main.holder_renter_node.*

class FileHolder(itemView: View, filesFragment: FilesFragment) : NodeHolder(itemView, filesFragment) {

    fun bind(file: SiaFile) {
        super.bind(file)

        file_redundancy.text = file.redundancy.format() + "x"

        val progress = file.uploadprogress.toInt()
        if (progress == 100) {
            file_upload_progressbar.gone()
            file_upload_progress_text.gone()
        } else {
            file_upload_progressbar.setProgressColorRes(if (file.available) R.color.colorPrimary else R.color.negativeTransaction)
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