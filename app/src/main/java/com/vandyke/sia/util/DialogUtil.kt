/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.support.v7.app.AlertDialog
import android.view.WindowManager

fun AlertDialog.Builder.showDialogAndKeyboard() {
    val dialog = this.create()
    dialog.show()
    dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
}