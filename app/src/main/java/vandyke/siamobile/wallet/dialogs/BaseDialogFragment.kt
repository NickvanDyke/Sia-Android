/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.dialogs

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import vandyke.siamobile.util.GenUtil

abstract class BaseDialogFragment : DialogFragment() {
    protected abstract val layout: Int
    protected var container: ViewGroup? = null

    abstract fun create(view: View?, savedInstanceState: Bundle?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.container = container
        return inflater.inflate(layout, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        create(view, savedInstanceState)
    }

    fun setCloseListener(view: View) {
        view.setOnClickListener { close() }
    }

    fun close() {
        container?.visibility = View.GONE
        if (dialog != null) dismiss()
        GenUtil.hideSoftKeyboard(activity)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // TODO: find some way to make dialog normal sized instead of wrap_content?
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.window.setTitle("Unlock walletModel")
        return dialog
    }
}