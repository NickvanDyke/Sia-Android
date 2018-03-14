package com.vandyke.sia.ui.node.settings

import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.util.getAllFilesDirs
import io.github.tonnyl.light.Light
import java.io.File

/* Note that we don't need to take manual action regarding siad when settings change, because SiadSource will
 * already be listening for changes to the relevant preferences. */
class NodeSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.node_settings)

        val storageDirs = context!!.getAllFilesDirs().map { it.absolutePath }.toTypedArray()
        val storagePref = findPreference("siaWorkingDirectory") as ListPreference

        storagePref.entries = storageDirs
        storagePref.entryValues = storageDirs
        val index = storageDirs.indexOfFirst { it == Prefs.siaWorkingDirectory }
        if (index != -1)
            storagePref.setValueIndex(index)

        storagePref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val dir = File(newValue as String)
            return@OnPreferenceChangeListener if (!dir.exists()) {
                Light.error(view!!, "Error: selected directory doesn't exist", Snackbar.LENGTH_LONG).show()
                false
            } else if (dir.absolutePath != context!!.filesDir.absolutePath) {
                if (Environment.getExternalStorageState(dir) != Environment.MEDIA_MOUNTED) {
                    Light.error(view!!, "Error with external storage: ${Environment.getExternalStorageState(dir)}", Snackbar.LENGTH_LONG).show()
                    false
                } else {
                    Light.success(view!!, "Changed Sia node's working directory, restarting it...", Snackbar.LENGTH_LONG).show()
                    true
                }
            } else {
                Light.success(view!!, "Changed Sia node's working directory, restarting it...", Snackbar.LENGTH_LONG).show()
                true
            }
        }
    }
}