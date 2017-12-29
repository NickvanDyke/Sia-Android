/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v7.preference.*
import vandyke.siamobile.BuildConfig
import vandyke.siamobile.R
import vandyke.siamobile.data.local.Prefs
import vandyke.siamobile.data.siad.SiadService

/* the actual settings fragment, contained within SettingsFragment */
class SettingsFragmentActual : PreferenceFragmentCompat() {

    private lateinit var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener

    private lateinit var siaNodeCategory: PreferenceCategory
    private lateinit var siaNodeWakeLock: SwitchPreferenceCompat

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        siaNodeCategory = findPreference("siaNodeCategory") as PreferenceCategory
        siaNodeWakeLock = findPreference("SiaNodeWakeLock") as SwitchPreferenceCompat
//        useExternal = findPreference("useExternal") as SwitchPreferenceCompat

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

        val decimalPrecision = findPreference("displayedDecimalPrecision") as EditTextPreference
        decimalPrecision.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            try {
                return@OnPreferenceChangeListener Integer.parseInt(newValue as String) < 10
            } catch (e: Exception) {
                return@OnPreferenceChangeListener false
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "SiaNodeWakeLock" -> SiadService.getService(context!!).subscribe { service ->
                    /* If Siad is already running then we must tell the service to acquire/release its wake lock
                       because normally it does so in start/stopSiad() */
                    if (service.isSiadProcessRunning) {
                        if (Prefs.SiaNodeWakeLock && !service.wakeLock.isHeld)
                            service.wakeLock.acquire()
                        else if (service.wakeLock.isHeld)
                            service.wakeLock.release()
                    }
                }
            }
        }
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }
}
