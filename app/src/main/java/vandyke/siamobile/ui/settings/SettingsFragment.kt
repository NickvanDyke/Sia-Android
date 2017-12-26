/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.settings

import android.os.Bundle
import android.view.View
import vandyke.siamobile.R
import vandyke.siamobile.ui.common.BaseFragment

/* a fragment that contains SettingsFragmentActual, since the actual settings fragment cannot extend BaseFragment */
class SettingsFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_settings_container
    private var settingsFragment: SettingsFragmentActual? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /* attempt to find the existing fragment, in case the activity is being recreated */
        settingsFragment = fragmentManager!!.findFragmentByTag("settingsFragment") as? SettingsFragmentActual
        if (settingsFragment == null) {
            settingsFragment = SettingsFragmentActual()
            childFragmentManager.beginTransaction().add(R.id.settings_fragment_frame, settingsFragment, "settingsFragment").commit()
        }

    }
}