package com.vandyke.sia.util

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context

object ClipboardServiceUtil {

    fun getPrimaryText(context: Context): String? {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE)
        if (clipboardManager is ClipboardManager
                && clipboardManager.hasPrimaryClip()
                && clipboardManager.primaryClipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
            return clipboardManager.primaryClip.getItemAt(0).text?.toString()

        return null
    }
}