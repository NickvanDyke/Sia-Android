/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs

object DialogUtil {
    fun showRateDialog(context: Context) {
        AlertDialog.Builder(context)
                .setTitle("Liking Sia for Android?")
                .setPositiveButton("Yes") { _, _ ->
                    AlertDialog.Builder(context)
                            .setMessage("Would you like to leave a rating? I'd really appreciate it.")
                            .setPositiveButton("Sure") { _, _ ->
                                Analytics.likingSiaForAndroid(true, true)
                                Toast.makeText(context, "Thank you :)", Toast.LENGTH_LONG).show()
                                context.startActivity(Intents.playStore)
                            }.setNegativeButton("No thanks") { _, _ ->
                                Analytics.likingSiaForAndroid(true, false)
                            }
                            .setCancelable(false)
                            .show()
                }
                /* the negative button is the middle one, which is where we want this, despite it being
                 * a more neutral option */
                .setNegativeButton("Ask again later") { _, _ ->
                    Prefs.shownFeedbackDialog = false
                }
                .setNeutralButton("No") { _, _ ->
                    AlertDialog.Builder(context)
                            .setMessage("Would you like to give feedback on why?")
                            .setPositiveButton("Yes") { _, _ ->
                                Analytics.likingSiaForAndroid(false, true)
                                context.startActivity(Intents.emailMe
                                        .putExtra(Intent.EXTRA_SUBJECT, "Feedback"))
                            }.setNegativeButton("No thanks") { _, _ ->
                                Analytics.likingSiaForAndroid(false, false)
                            }
                            .setCancelable(false)
                            .show()
                }
                .setCancelable(false)
                .show()
    }

    fun editTextDialog(
            context: Context,
            title: String,
            positiveText: String? = null,
            positiveFunc: ((String) -> Unit)? = null,
            negativeText: String? = null,
            negativeFunc: ((String) -> Unit)? = null,
            editTextFunc: (EditText.() -> Unit)? = null
    ): AlertDialog {
        val view = View.inflate(context, R.layout.edit_text_field, null)
        val editText = view.findViewById<EditText>(R.id.field)
        val dialog = with(AlertDialog.Builder(context)) {
            setTitle(title)
            setView(view)
            if (editTextFunc != null) {
                editText.editTextFunc()
            }
            positiveText?.let { setPositiveButton(it, { _, _ -> positiveFunc?.invoke(editText.text.toString()) }) }
            negativeText?.let { setNegativeButton(it, { _, _ -> negativeFunc?.invoke(editText.text.toString()) }) }
            create()
        }
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.dismiss()
                positiveFunc?.invoke(editText.text.toString())
            }
            true
        }
        return dialog
    }

    fun editTextSpinnerDialog(
            context: Context,
            title: String,
            positiveText: String? = null,
            positiveFunc: ((String, String) -> Unit)? = null,
            negativeText: String? = null,
            negativeFunc: ((String) -> Unit)? = null,
            editTextFunc: (EditText.() -> Unit)? = null,
            spinnerItems: List<String>
    ): AlertDialog {
        val view = View.inflate(context, R.layout.edit_text_spinner, null)
        val editText = view.findViewById<EditText>(R.id.field)
        val spinner = view.findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter<String>(context, R.layout.spinner_selected_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val dialog = with(AlertDialog.Builder(context)) {
            setTitle(title)
            setView(view)
            if (editTextFunc != null) {
                editText.editTextFunc()
            }
            adapter.addAll(spinnerItems)
            positiveText?.let { setPositiveButton(it, { _, _ -> positiveFunc?.invoke(editText.text.toString(), spinner.selectedItem as String) }) }
            negativeText?.let { setNegativeButton(it, { _, _ -> negativeFunc?.invoke(editText.text.toString()) }) }
            create()
        }
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.dismiss()
                positiveFunc?.invoke(editText.text.toString(), spinner.selectedItem as String)
            }
            true
        }
        return dialog
    }
}

fun AlertDialog.Builder.showDialogAndKeyboard() {
    this.show().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
}

fun AlertDialog.showDialogAndKeyboard() {
    this.show()
    this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
}