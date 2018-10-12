/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.fileupload

import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.name
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.getColorRes
import com.vandyke.sia.util.gone
import com.vandyke.sia.util.visible
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_file_upload.*

class FileUploadHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), LayoutContainer {
    override val containerView: View? = itemView

    fun bind(upload: FileUpload) {
        file_upload_name.text = upload.path.name()
        if (upload.size != 0L) {
            // TODO: set color. Need to get attr differently since it's a ColorStateList
//            file_upload_name.setTextColor(itemView.context.getAttrColor(android.R.attr.textColorPrimary))
            file_upload_size.text = StorageUtil.readableFilesizeString(upload.size)
            file_upload_size.visible()
        } else {
            file_upload_name.setTextColor(itemView.context.getColorRes(R.color.color_error))
            file_upload_size.gone()
        }
    }
}