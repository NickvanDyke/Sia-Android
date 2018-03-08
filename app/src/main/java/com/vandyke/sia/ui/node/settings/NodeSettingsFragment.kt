package com.vandyke.sia.ui.node.settings

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import com.vandyke.sia.R
import com.vandyke.sia.util.ExternalStorageException
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.snackbar

/* Note that we don't need to take manual action regarding siad when settings change, because SiadSource will
 * already be listening for changes to the relevant preferences. */
class NodeSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.node_settings)

        findPreference("useExternal").onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                try {
                    StorageUtil.getExternalStorage(context!!)
                    return@OnPreferenceChangeListener true
                } catch (e: ExternalStorageException) {
                    e.snackbar(view!!, Snackbar.LENGTH_LONG)
                    return@OnPreferenceChangeListener false
                }
            } else {
                return@OnPreferenceChangeListener true
            }
        }
    }
}