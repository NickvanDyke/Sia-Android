/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vandyke.sia.R
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.observe
import kotlinx.android.synthetic.main.fragment_renter_dir_more.*

class DirBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_renter_dir_more, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel = ViewModelProviders.of(parentFragment!!).get(FilesViewModel::class.java)
        viewModel.detailsItem.observe(this) { dir ->
            if (dir !is Dir)
                throw Exception()
            /* set the value to null, because it's been displayed and we don't want it displayed another time */
            viewModel.detailsItem.value = null
            dirDelete.setOnClickListener {
                viewModel.deleteDir(dir.path)
                dismiss()
            }
        }
    }
}