/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.settings

import com.vandyke.sia.R
import com.vandyke.sia.ui.common.BaseFragment

/* a fragment that contains SettingsFragment, since the actual settings fragment cannot extend
 * both PreferenceFragment and BaseFragment */
class SettingsFragmentContainer : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_settings_container
    override val title: String = "Settings"
}