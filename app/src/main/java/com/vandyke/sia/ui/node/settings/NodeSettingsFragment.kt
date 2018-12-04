package com.vandyke.sia.ui.node.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.takisoft.preferencex.PreferenceFragmentCompat
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadStatus
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.util.getAllFilesDirs
import io.github.tonnyl.light.Light
import java.io.File
import javax.inject.Inject


/* Note that we don't need to take manual action regarding siad when settings change, because SiadSource will
 * already be listening for changes to the relevant preferences. */
class NodeSettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var siadStatus: SiadStatus

    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        context!!.getAppComponent().inject(this)

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
                Light.error(view!!, "Error: selected directory doesn't exist", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
                false
            } else if (dir.absolutePath != context!!.filesDir.absolutePath) {
                if (Environment.getExternalStorageState(dir) != Environment.MEDIA_MOUNTED) {
                    Light.error(view!!, "Error with external storage: ${Environment.getExternalStorageState(dir)}", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
                    false
                } else {
                    successSnackbar("Changed Sia node's working directory")
                    true
                }
            } else {
                successSnackbar("Changed Sia node's working directory")
                true
            }
        }

        findPreference("apiPassword").onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            // for some reason siad gives incorrect API password error when we have an empty password set. So don't allow that.
            (newValue as String).isNotEmpty()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "apiPassword" -> successSnackbar("Changed Sia node's API password")
            }
        }
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        Prefs.preferences.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun successSnackbar(text: String) {
        var msg = text
        if (siadStatus.state.value!!.processIsRunning)
            msg += ", restarting it..."
        Light.success(view!!, msg, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
    }
}