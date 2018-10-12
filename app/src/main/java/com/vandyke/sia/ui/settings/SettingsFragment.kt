/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import com.takisoft.preferencex.PreferenceFragmentCompat
import com.vandyke.sia.BuildConfig
import com.vandyke.sia.R
import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.ui.main.MainActivity
import com.vandyke.sia.ui.terminal.TerminalFragment
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.rx.io
import com.vandyke.sia.util.rx.main
import io.github.tonnyl.light.Light
import io.reactivex.Completable
import javax.inject.Inject

/* the actual settings fragment, contained within SettingsFragmentContainer */
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var db: AppDatabase

    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        context!!.getAppComponent().inject(this)

        addPreferencesFromResource(R.xml.app_settings)

        findPreference("displayedDecimalPrecision").onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            try {
                return@OnPreferenceChangeListener Integer.parseInt(newValue as String) < 10
            } catch (e: Exception) {
                return@OnPreferenceChangeListener false
            }
        }

        findPreference("openAppSettings").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val appSettings = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID))
            appSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(appSettings)
            false
        }

        findPreference("viewSubscription").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            GenUtil.launchCustomTabs(context!!, "https://play.google.com/store/account/subscriptions")
            false
        }

        findPreference("openTerminal").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            (activity as MainActivity).apply {
                displayFragment(TerminalFragment::class.java)
                deselectDrawer()
            }
            false
        }

        findPreference("clearDatabase").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Completable.fromAction { db.clearAllTables() }
                    .io()
                    .main()
                    .subscribe({ Light.success(view!!, "Cleared cached data", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show() },
                            { Light.success(view!!, "Error clearing cached data: ${it.localizedMessage}", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show() })
            false
        }

        findPreference("resetPreferences").onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if (Prefs.preferences.edit().clear().commit())
                Light.success(view!!, "Reset preferences. Restart app to take effect.", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
            else
                Light.error(view!!, "Error resetting preferences", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
            false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "darkMode", "oldSiaColors" -> activity!!.recreate()
            }
        }
        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        Prefs.preferences.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }
}
