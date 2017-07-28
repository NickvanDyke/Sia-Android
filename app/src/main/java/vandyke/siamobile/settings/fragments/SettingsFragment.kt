/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.settings.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.preference.*
import android.support.design.widget.Snackbar
import android.util.Base64
import vandyke.siamobile.BuildConfig
import vandyke.siamobile.R
import vandyke.siamobile.SiaMobileApplication
import vandyke.siamobile.misc.Utils
import vandyke.siamobile.prefs
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SettingsFragment : PreferenceFragment() {
    private val SELECT_PICTURE = 1
    private lateinit var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener

    private lateinit var operation: PreferenceCategory
    private lateinit var operationMode: ListPreference
    private lateinit var remoteAddress: EditTextPreference
    private lateinit var apiPass: EditTextPreference
    private lateinit var runLocalNodeOffWifi: SwitchPreference
    private lateinit var useExternal: SwitchPreference
    private lateinit var minBattery: EditTextPreference
    private lateinit var runInBackground: SwitchPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)

        operation = findPreference("operationCategory") as PreferenceCategory
        remoteAddress = findPreference("remoteAddress") as EditTextPreference
        apiPass = findPreference("apiPass") as EditTextPreference
        runLocalNodeOffWifi = findPreference("runLocalNodeOffWifi") as SwitchPreference
        useExternal = findPreference("useExternal") as SwitchPreference
        minBattery = findPreference("localNodeMinBattery") as EditTextPreference
        runInBackground = findPreference("runInBackground") as SwitchPreference
        setColdStorageSettingsVisibility()
        setRemoteSettingsVisibility()
        setLocalSettingsVisibility()

        operationMode = findPreference("operationMode") as ListPreference
        operationMode.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
            if (o as String == "local_full_node" && SiaMobileApplication.abi != "arm64") {
                Utils.snackbar(view, "Sorry, but your device's CPU architecture is not supported by Sia's full node", Snackbar.LENGTH_LONG)
                return@OnPreferenceChangeListener false
            }
            true
        }

        useExternal.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
            if (Utils.isExternalStorageWritable)
                return@OnPreferenceChangeListener true
            else
                Utils.snackbar(view, "Error: " + Utils.externalStorageStateDescription(), Snackbar.LENGTH_LONG)
            false
        }

        val openAppSettings = findPreference("openAppSettings")
        openAppSettings.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val appSettings = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID))
            appSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(appSettings)
            false
        }

        val decimalPrecision = findPreference("displayedDecimalPrecision") as EditTextPreference
        decimalPrecision.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue -> newValue != "" }

        when (prefs.operationMode) {
            "cold_storage" -> operationMode.summary = "Cold storage"
            "remote_full_node" -> operationMode.summary = "Remote full node"
            "local_full_node" -> operationMode.summary = "Local full node"
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "operationMode" -> {
                    setColdStorageSettingsVisibility()
                    setRemoteSettingsVisibility()
                    setLocalSettingsVisibility()
                    when (prefs.operationMode) {
                        "cold_storage" -> {
                            operationMode.summary = "Cold storage"
                            operationMode.setValueIndex(0)
                        }
                        "remote_full_node" -> {
                            operationMode.summary = "Remote full node"
                            operationMode.setValueIndex(1)
                        }
                        "local_full_node" -> {
                            operationMode.summary = "Local full node"
                            operationMode.setValueIndex(2)
                        }
                    }
                }
                "monitorRefreshInterval" -> if (prefs.refreshInterval == 0)
                    operation.removePreference(runInBackground)
                else
                    operation.addPreference(runInBackground)
                "theme" -> if (prefs.theme == "custom") {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Select Background"), SELECT_PICTURE)
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageURI = data.data
                var input: InputStream? = null
                try {
                    input = activity.contentResolver.openInputStream(selectedImageURI)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

                val bitmap = BitmapFactory.decodeStream(input, null, null)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val b = baos.toByteArray()
                prefs.customBgBase64 = Base64.encodeToString(b, Base64.DEFAULT)
            }
        }
    }

    private fun setColdStorageSettingsVisibility() {

    }

    private fun setRemoteSettingsVisibility() {
        if (prefs.operationMode == "remote_full_node") {
            operation.addPreference(remoteAddress)
            operation.addPreference(apiPass)
        } else {
            operation.removePreference(remoteAddress)
            operation.removePreference(apiPass)
        }
    }

    private fun setLocalSettingsVisibility() {
        if (prefs.operationMode == "local_full_node") {
            operation.addPreference(runLocalNodeOffWifi)
            operation.addPreference(useExternal)
            operation.addPreference(minBattery)
        } else {
            operation.removePreference(runLocalNodeOffWifi)
            operation.removePreference(useExternal)
            operation.removePreference(minBattery)
        }
    }
}
