package com.vandyke.sia.ui.node

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadService
import com.vandyke.sia.util.SnackbarUtil

class NodeSettingsFragment : PreferenceFragmentCompat() {

    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.node_settings)

        findPreference("useExternal").onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (newValue as Boolean) {
                val dirs = context!!.getExternalFilesDirs(null)
                if (dirs.isEmpty()) {
                    SnackbarUtil.showSnackbar(view, "No external storage found")
                    return@OnPreferenceChangeListener false
                }
                val dir = if (dirs.size > 1) dirs[1] else dirs[0]
                val state = Environment.getExternalStorageState(dir)
                if (state == Environment.MEDIA_MOUNTED) {
                    return@OnPreferenceChangeListener true
                } else {
                    SnackbarUtil.showSnackbar(view, "Error with external storage: $state", Snackbar.LENGTH_LONG)
                    return@OnPreferenceChangeListener false
                }
            } else {
                return@OnPreferenceChangeListener true
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /* create and register a SharedPrefs PreferenceChangeListener that'll take appropriate action
         * when certain settings are changed. We are supposed to keep a reference, otherwise it could
         * be unregistered/destroyed. */
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "apiPassword", "useExternal" -> SiadService.getService(context!!).subscribe { service ->
                    service.restartSiad()
                }

                "darkMode" -> activity!!.recreate()
            }
        }
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }
}