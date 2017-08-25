/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.about

import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import vandyke.siamobile.R
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.backend.networking.Wallet
import vandyke.siamobile.util.GenUtil
import vandyke.siamobile.util.SnackbarUtil
import vandyke.siamobile.util.toHastings

class DonateDialog : DialogFragment() {

    private val paymentRecipient = GenUtil.devAddresses[(Math.random() * GenUtil.devAddresses.size).toInt()]

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = GenUtil.getDialogBuilder(activity)

        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_donate, null)

        dialogView.findViewById<Button>(R.id.donateButton).setOnClickListener {
            Wallet.send(dialogView.findViewById<EditText>(R.id.donateAmount).text.toString().toHastings().toPlainString(), paymentRecipient, SiaCallback({ ->
                SnackbarUtil.snackbar(dialogView, "Donation successful. Thank you!", Snackbar.LENGTH_SHORT)
            }, {
                SnackbarUtil.snackbar(dialogView, it.reason.msg + ". No donation made.", Snackbar.LENGTH_SHORT)
            }))
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
