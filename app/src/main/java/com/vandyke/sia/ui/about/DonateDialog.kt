/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.about

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.vandyke.sia.R
import com.vandyke.sia.data.remote.siaApi
import com.vandyke.sia.util.*

class DonateDialog : DialogFragment() {

    private val paymentRecipient = GenUtil.devAddresses[(Math.random() * GenUtil.devAddresses.size).toInt()]

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)

        val dialogView = activity!!.layoutInflater.inflate(R.layout.dialog_donate, null)

        dialogView.findViewById<Button>(R.id.donateButton).setOnClickListener {
            val amount = dialogView.findViewById<EditText>(R.id.donateAmount).text.toString().toHastings().toPlainString()
            siaApi.walletSiacoins(amount, paymentRecipient).io().main().subscribe({
                SnackbarUtil.showSnackbar(dialogView, "Donation successful. Thank you!", Snackbar.LENGTH_SHORT)
            }, {
                SnackbarUtil.showSnackbar(dialogView, it.message + ". No donation made.", Snackbar.LENGTH_SHORT)
            })
        }
        builder.setTitle("Donate")
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
            DonateDialog().show(fragmentManager, "donate dialog")
        }
    }
}
