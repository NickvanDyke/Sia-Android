/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.renter.files.view

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_renter_file_more.*
import vandyke.siamobile.R
import vandyke.siamobile.data.local.data.File
import vandyke.siamobile.ui.renter.files.viewmodel.FilesViewModel
import vandyke.siamobile.util.observe

class FileBottomSheetFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_renter_file_more, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel = ViewModelProviders.of(parentFragment!!).get(FilesViewModel::class.java)
        viewModel.detailsItem.observe(this) { file ->
            if (file !is File)
                throw Exception()
            /* set the value to null, because it's been displayed and we don't want it displayed another time */
            viewModel.detailsItem.value = null
            fileDelete.setOnClickListener {
                viewModel.deleteFile(file)
                dismiss()
            }
        }
    }
}