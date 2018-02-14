/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.vandyke.sia.R

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
                }.setNegativeButton("No") { _, _ ->
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
                }.setCancelable(false)
                .show()
    }

    fun editTextDialog(context: Context, title: String, hint: String,
                       positiveText: String? = null, positiveFunc: ((EditText) -> Unit)? = null,
                       negativeText: String? = null, negativeFunc: ((EditText) -> Unit)? = null): AlertDialog.Builder {
        return with(AlertDialog.Builder(context)) {
            setTitle(title)
            val view = View.inflate(context, R.layout.edit_text_field, null)
            setView(view)
            val editText = view.findViewById<EditText>(R.id.field)
            editText.hint = hint
            positiveText?.let { setPositiveButton(it, { _, _ -> positiveFunc?.invoke(editText) }) }
            negativeText?.let { setNegativeButton(it, { _, _ -> negativeFunc?.invoke(editText) }) }
            this
        }
    }
}

fun AlertDialog.Builder.showDialogAndKeyboard() {
    val dialog = this.create()
    dialog.show()
    dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
}

fun AlertDialog.showDialogAndKeyboard() {
    this.show()
    this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
}