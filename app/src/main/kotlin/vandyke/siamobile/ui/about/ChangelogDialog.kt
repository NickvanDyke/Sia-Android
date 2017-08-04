package vandyke.siamobile.ui.about

import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.os.Bundle
import android.view.WindowManager
import vandyke.siamobile.R
import vandyke.siamobile.util.GenUtil

class ChangelogDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = GenUtil.getDialogBuilder(activity)

        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_changelog, null)

        builder.setTitle("Change Log")
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
            ChangelogDialog().show(fragmentManager, "changelog dialog")
        }
    }
}