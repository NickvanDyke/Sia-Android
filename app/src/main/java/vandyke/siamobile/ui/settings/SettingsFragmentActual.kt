/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v7.preference.*
import vandyke.siamobile.BuildConfig
import vandyke.siamobile.R
import vandyke.siamobile.ui.MainActivity

/* the actual settings fragment, contained within SettingsFragment */
class SettingsFragmentActual : PreferenceFragmentCompat() {
    private val SELECT_PICTURE = 1
    private lateinit var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener

    private lateinit var operation: PreferenceCategory
    private lateinit var operationMode: ListPreference
    private lateinit var remoteAddress: EditTextPreference
    private lateinit var apiPass: EditTextPreference
    private lateinit var siaNodeWakeLock: SwitchPreferenceCompat
    private lateinit var runLocalNodeOffWifi: SwitchPreferenceCompat
    private lateinit var useExternal: SwitchPreferenceCompat
    private lateinit var minBattery: EditTextPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        operation = findPreference("operationCategory") as PreferenceCategory
        operationMode = findPreference("operationMode") as ListPreference
        remoteAddress = findPreference("remoteAddress") as EditTextPreference
        apiPass = findPreference("apiPass") as EditTextPreference
        runLocalNodeOffWifi = findPreference("runLocalNodeOffWifi") as SwitchPreferenceCompat
        siaNodeWakeLock = findPreference("SiaNodeWakeLock") as SwitchPreferenceCompat
//        useExternal = findPreference("useExternal") as SwitchPreferenceCompat
        minBattery = findPreference("localNodeMinBattery") as EditTextPreference
        setRemoteSettingsVisibility()
        setLocalSettingsVisibility()

        operationMode.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
            if (o == "view_explanation") {
                activity!!.startActivityForResult(Intent(activity, ModesActivity::class.java), MainActivity.REQUEST_OPERATION_MODE)
                return@OnPreferenceChangeListener false
            }
            return@OnPreferenceChangeListener true
        }

//        useExternal.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
//            if (StorageUtil.isExternalStorageWritable) {
//                return@OnPreferenceChangeListener true
//            } else {
//                SnackbarUtil.snackbar(view, "Error: " + StorageUtil.externalStorageStateDescription(), Snackbar.LENGTH_LONG)
//                return@OnPreferenceChangeListener false
//            }
//        }

        findPreference("openAppSettings").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val appSettings = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID))
            appSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(appSettings)
            false
        }

        minBattery.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            try {
                return@OnPreferenceChangeListener Integer.parseInt(newValue as String) <= 100
            } catch (e: Exception) {
                return@OnPreferenceChangeListener false
            }
        }

        val decimalPrecision = findPreference("displayedDecimalPrecision") as EditTextPreference
        decimalPrecision.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            try {
                return@OnPreferenceChangeListener Integer.parseInt(newValue as String) < 10
            } catch (e: Exception) {
                return@OnPreferenceChangeListener false
            }
        }

        when (Prefs.operationMode) {
            "remote_full_node" -> operationMode.summary = "Remote full node"
            "local_full_node" -> operationMode.summary = "Local full node"
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "operationMode" -> {
                    setRemoteSettingsVisibility()
                    setLocalSettingsVisibility()
                    when (Prefs.operationMode) {
                        "local_full_node" -> {
                            operationMode.summary = "Local full node"
                            operationMode.setValueIndex(1)
                        }
                        "remote_full_node" -> {
                            operationMode.summary = "Remote full node"
                            operationMode.setValueIndex(2)
                        }
                    }
                }
//                "darkMode" -> { // TODO: maybe eventually work on having it change in-app without having to restart
//                    if (Prefs.darkMode)
//                        (activity!! as AppCompatActivity).delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                    else
//                        (activity!! as AppCompatActivity).delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                }
            }
        }
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun setRemoteSettingsVisibility() {
        if (Prefs.operationMode == "remote_full_node") {
            operation.addPreference(remoteAddress)
            operation.addPreference(apiPass)
        } else {
            operation.removePreference(remoteAddress)
            operation.removePreference(apiPass)
        }
    }

    private fun setLocalSettingsVisibility() {
        if (Prefs.operationMode == "local_full_node") {
            operation.addPreference(siaNodeWakeLock)
            operation.addPreference(runLocalNodeOffWifi)
//            operation.addPreference(useExternal)
            operation.addPreference(minBattery)
        } else {
            operation.removePreference(siaNodeWakeLock)
            operation.removePreference(runLocalNodeOffWifi)
//            operation.removePreference(useExternal)
            operation.removePreference(minBattery)
        }
    }
}
