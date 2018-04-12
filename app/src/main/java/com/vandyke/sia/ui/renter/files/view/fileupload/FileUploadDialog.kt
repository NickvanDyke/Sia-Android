/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view.fileupload

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.util.FileUtils
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.visible
import kotlinx.android.synthetic.main.dialog_file_upload.view.*
import java.io.File

class FileUploadDialog : DialogFragment() {

    private lateinit var uris: List<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uris = arguments!!.getParcelableArray(URIS_KEY).toList() as List<Uri>
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_file_upload, null, false)

        var totalSize = 0L
        val uploads = uris.map {
            val path = FileUtils.getPath(context!!, it)
            if (path != null) {
                val size = File(path).length()
                totalSize += size.coerceAtLeast(1024 * 1024 * 40) // Sia's 40MB minimum filesize
                FileUpload(path, size)
            } else {
                FileUpload(it.toString(), 0L)
            }
        }

        view.uploading_files_list.adapter = FileUploadAdapter(uploads)
        view.redundancy_input.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                view.file_upload_total_size.text = try {
                    StorageUtil.readableFilesizeString((s.toString().toFloat() * totalSize).toLong())
                } catch (e: NumberFormatException) {
                    "0 B"
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
        })

        view.redundancy_input.setText(Prefs.redundancy.toString())

        if (uploads.any { it.size == 0L }) {
            view.unsupported_source_icon.visible()
            view.unsupported_source_warning.visible()
        }

        return AlertDialog.Builder(context!!)
                .setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Upload") { _, _ ->
                    val redundancy = try {
                        view.redundancy_input.text.toString().toFloat()
                    } catch (e: NumberFormatException) {
                        0f
                    }
                    Prefs.redundancy = redundancy
                    val intent = Intent()
                            .putExtra(REDUNDANCY_KEY, redundancy)
                            .putExtra(UPLOADS_KEY, uploads.filter { it.size != 0L }.map { it.path }.toTypedArray())
                    targetFragment!!.onActivityResult(targetRequestCode, RESULT_OK, intent)
                }
                .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_file_upload, container, false)
    }

    companion object {
        private const val URIS_KEY = "URIS_KEY"

        const val UPLOADS_KEY = "UPLOADS_KEY"
        const val REDUNDANCY_KEY = "REDUNDANCY_KEY"

        fun newInstance(uris: List<Uri>): FileUploadDialog {
            return FileUploadDialog().apply {
                arguments = Bundle().apply { putParcelableArray(URIS_KEY, uris.toTypedArray()) }
            }
        }
    }
}