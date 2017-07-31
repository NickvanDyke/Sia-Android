package vandyke.siamobile.util

import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import vandyke.siamobile.R

object SnackbarUtil {
    fun snackbar(view: View?, text: String, duration: Int) {
        if (view == null || !view.isShown)
            return
        val snackbar = Snackbar.make(view, text, duration)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.colorAccent))
        snackbar.show()
    }

    fun successSnackbar(view: View?) {
        snackbar(view, "Success", Snackbar.LENGTH_SHORT)
    }
}