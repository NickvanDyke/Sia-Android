package vandyke.siamobile.ui

import android.support.v4.app.Fragment

abstract class BaseFragment : Fragment() {
    open fun onBackPressed(): Boolean = false

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden)
            activity!!.invalidateOptionsMenu()
    }
}