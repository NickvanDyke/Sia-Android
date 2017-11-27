/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.about

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.WindowManager
import vandyke.siamobile.R

class ChangelogDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)

        val dialogView = activity!!.layoutInflater!!.inflate(R.layout.dialog_changelog, null)

        builder.setTitle("Change Log")
                .setView(dialogView)
                .setPositiveButton("Close", null)
        return builder.create()
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        return inflater.inflate(R.layout.dialog_donate, null)
//    }

    override fun onActivityCreated(bundle: Bundle?) {
        super.onActivityCreated(bundle)
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    companion object {
        fun createAndShow(fragmentManager: FragmentManager) {
            ChangelogDialog().show(fragmentManager, "changelog dialog")
        }
    }
}