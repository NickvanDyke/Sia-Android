/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.util

import android.app.Activity
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import vandyke.siamobile.ui.settings.Prefs
import java.math.BigDecimal
import java.math.RoundingMode


object GenUtil {
    val devAddresses by lazy { listOf("20c9ed0d1c70ab0d6f694b7795bae2190db6b31d97bc2fba8067a336ffef37aacbc0c826e5d3",
            "36ab7ac91b981f998a0f5417b7f64299375cc5ffe096841044597b48346936b49741bfeb6cf5",
            "65cc0ab13a1ccb7788cf36554daf980f162c5bf2fec9a3664192916b26c568af4eda38f666d0",
            "870878df29ee72082673ddf1e53f5ed2f52a8e84486d85e241ee531c1350066ad9622ed0ec61",
            "8a9d8e6c8d043300b967443eaaa01874efa36e69a95b03c6b970bfe5b82a7f0345424a7919af",
            "986082d52bf8a25009e7ce97508385687f3241d1e027969edbf9f63e4240cecf77bad58f40a5",
            "a58c0c63ec11b8b7b6410e76920882ea312a6762c2da98e0376ee96a8e23392f9df4e403c584",
            "a61260748a55cdbed8c28038724ada4c5284062ae799df530147aa9b8809c929145585a83cdb",
            "b05b1603c8e640a6617107d3f8f90925c13d98213822afee0c481022ef236ee9bae778ea2971",
            "ca4e94a53e257fcac10d8890aa76d73bf6a6490686301232236f8d99c4dedc1158857cf6c558",
            "f39caefc5e7f5f92a3e13a04837524a8096bc3873f551e3bd1f6c6c4cff2d2c664ddf6cfa27f")}

    fun hideSoftKeyboard(activity: Activity?) {
        if (activity == null)
            return
        val inputMethodManager = activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus.windowToken, 0)
    }

    fun hideSoftKeyboard(view: View?) {
        if (view == null)
            return
        val inputMethodManager = view.context.getSystemService(
                Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
                view.applicationWindowToken, 0)
    }

    fun copyToClipboard(context: Context, text: CharSequence) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Sia Mobile copy", text)
        clipboard.primaryClip = clip
    }

    fun readableFilesizeString(filesize: BigDecimal): String {
        var size = filesize
        var i = 0
        val kilo = BigDecimal("1024")
        while (size > kilo && i < 6) {
            size = size.divide(kilo, 10, RoundingMode.HALF_UP)
            i++
        }
        val sizeString: String
        when (i) {
            0 -> sizeString = "B"
            1 -> sizeString = "KB"
            2 -> sizeString = "MB"
            3 -> sizeString = "GB"
            4 -> sizeString = "TB"
            5 -> sizeString = "PB"
            6 -> sizeString = "EB"
            else -> sizeString = "Super big"
        }

        return String.format("%.${Prefs.displayedDecimalPrecision}f %s", size, sizeString)
    }
}

fun <T> LiveData<T>.observe(owner: LifecycleOwner, onChanged: (T) -> Unit) {
    this.observe(owner, Observer {
        if (it != null)
            onChanged(it)
    })
}