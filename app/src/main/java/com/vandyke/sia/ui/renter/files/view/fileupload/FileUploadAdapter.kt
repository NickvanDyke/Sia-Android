/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.fileupload

import android.view.LayoutInflater
import android.view.ViewGroup
import com.vandyke.sia.R

class FileUploadAdapter(private val uploads: List<FileUpload>) : androidx.recyclerview.widget.RecyclerView.Adapter<FileUploadHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileUploadHolder {
        return FileUploadHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_file_upload, parent, false))
    }

    override fun onBindViewHolder(holder: FileUploadHolder, position: Int) {
        holder.bind(uploads[position])
    }

    override fun getItemCount() = uploads.size
}

