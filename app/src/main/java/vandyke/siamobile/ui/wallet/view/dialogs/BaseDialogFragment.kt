/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.view.dialogs

import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import vandyke.siamobile.ui.wallet.viewmodel.WalletViewModel
import vandyke.siamobile.util.GenUtil

abstract class BaseDialogFragment : DialogFragment() {
    protected abstract val layout: Int
    protected var container: ViewGroup? = null
    protected lateinit var viewModel: WalletViewModel

    abstract fun create(view: View?, savedInstanceState: Bundle?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.container = container
        val view = inflater.inflate(layout, null)
        /* set this because the first touch listener to return true receives the rest of the touch events,
           which we want so that the SwipeableFrameLayout can intercept them and determine swipes */
        view.setOnTouchListener { v, event ->
            true
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(parentFragment!!).get(WalletViewModel::class.java)
        create(view, savedInstanceState)
    }

    fun setCloseButton(view: View) {
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
//        dialog.window.setTitle("Unlock model")
        return dialog
    }
}