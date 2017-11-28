package vandyke.siamobile.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import vandyke.siamobile.R
import vandyke.siamobile.ui.BaseFragment

/* a fragment that contains SettingsFragmentActual, since the actual settings fragment cannot extend BaseFragment */
class SettingsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings_container, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.beginTransaction().add(R.id.settings_fragment_frame, SettingsFragmentActual()).commit()
    }
}