/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.renter.view

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_renter_dir_more.*
import vandyke.siamobile.R
import vandyke.siamobile.data.local.data.Dir
import vandyke.siamobile.ui.renter.viewmodel.RenterViewModel
import vandyke.siamobile.util.observe

class DirBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_renter_dir_more, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel = ViewModelProviders.of(parentFragment!!).get(RenterViewModel::class.java)
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