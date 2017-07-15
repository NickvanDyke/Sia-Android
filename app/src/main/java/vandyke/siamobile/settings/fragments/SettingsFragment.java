/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.settings.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import vandyke.siamobile.BuildConfig;
import vandyke.siamobile.R;
import vandyke.siamobile.SiaMobileApplication;
import vandyke.siamobile.misc.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    private PreferenceCategory operation;
    private ListPreference operationMode;
    private EditTextPreference remoteAddress;
    private EditTextPreference apiPass;
    private SwitchPreference runLocalNodeOffWifi;
    private SwitchPreference useExternal;
    private EditTextPreference minBattery;
    private SwitchPreference runInBackground;

    private static final int SELECT_PICTURE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        operation = (PreferenceCategory) findPreference("operationCategory");
        remoteAddress = (EditTextPreference) findPreference("remoteAddress");
        apiPass = (EditTextPreference) findPreference("apiPass");
        runLocalNodeOffWifi = (SwitchPreference) findPreference("runLocalNodeOffWifi");
        useExternal = (SwitchPreference) findPreference("useExternal");
        minBattery = (EditTextPreference) findPreference("localNodeMinBattery");
        runInBackground = (SwitchPreference) findPreference("runInBackground");
        setColdStorageSettingsVisibility();
        setRemoteSettingsVisibility();
        setLocalSettingsVisibility();

        operationMode = (ListPreference) findPreference("operationMode");
        operationMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (((String) o).equals("local_full_node")
                        && !(SiaMobileApplication.abi.equals("arm64"))) {
                    Utils.snackbar(getView(), "Sorry, but your device's CPU architecture is not supported by Sia's full node", Snackbar.LENGTH_LONG);
                    return false;
                }
                return true;
            }
        });

        useExternal.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (Utils.isExternalStorageWritable())
                    return true;
                else
                    Utils.snackbar(getView(), "Error: " + Utils.externalStorageStateDescription(), Snackbar.LENGTH_LONG);
                return false;
            }
        });

        final Preference openAppSettings = findPreference("openAppSettings");
        openAppSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent appSettings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                appSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(appSettings);
                return false;
            }
        });

        final EditTextPreference decimalPrecision = (EditTextPreference) findPreference("displayedDecimalPrecision");
        decimalPrecision.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return !newValue.equals("");
            }
        });

        switch (SiaMobileApplication.prefs.getString("operationMode", "cold_storage")) {
            case "cold_storage":
                operationMode.setSummary("Cold storage");
                break;
            case "remote_full_node":
                operationMode.setSummary("Remote full node");
                break;
            case "local_full_node":
                operationMode.setSummary("Local full node");
                break;
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key) {
                    case "operationMode":
                        setColdStorageSettingsVisibility();
                        setRemoteSettingsVisibility();
                        setLocalSettingsVisibility();
                        if (sharedPreferences.getString("operationMode", "cold_storage").equals("cold_storage")) {
                            operationMode.setSummary("Cold storage");
                            operationMode.setValueIndex(0);
                        } else if (sharedPreferences.getString("operationMode", "cold_storage").equals("remote_full_node")) {
                            operationMode.setSummary("Remote full node");
                            operationMode.setValueIndex(1);
                        } else if (sharedPreferences.getString("operationMode", "cold_storage").equals("local_full_node")) {
                            operationMode.setSummary("Local full node");
                            operationMode.setValueIndex(2);
                        }
                        break;
                    case "monitorRefreshInterval":
                        if (Integer.parseInt(sharedPreferences.getString("monitorRefreshInterval", "1")) == 0)
                            operation.removePreference(runInBackground);
                        else
                            operation.addPreference(runInBackground);
                        break;
                    case "theme":
                        switch (sharedPreferences.getString("theme", "light")) {
                            case "custom":
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(Intent.createChooser(intent, "Select Background"), SELECT_PICTURE);
                                break;
                        }
                        break;
                }
            }
        };
        SiaMobileApplication.prefs.registerOnSharedPreferenceChangeListener(prefsListener);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageURI = data.getData();
                InputStream input = null;
                try {
                    input = getActivity().getContentResolver().openInputStream(selectedImageURI);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(input, null, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();
                SharedPreferences.Editor prefsEditor = SiaMobileApplication.prefs.edit();
                prefsEditor.putString("customBgBase64", Base64.encodeToString(b, Base64.DEFAULT));
                prefsEditor.apply();
            }
        }
    }

    private void setColdStorageSettingsVisibility() {

    }

    private void setRemoteSettingsVisibility() {
        if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("remote_full_node")) {
            operation.addPreference(remoteAddress);
            operation.addPreference(apiPass);
        } else {
            operation.removePreference(remoteAddress);
            operation.removePreference(apiPass);
        }
    }

    private void setLocalSettingsVisibility() {
        if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("local_full_node")) {
            operation.addPreference(runLocalNodeOffWifi);
            operation.addPreference(useExternal);
            operation.addPreference(minBattery);
        } else {
            operation.removePreference(runLocalNodeOffWifi);
            operation.removePreference(useExternal);
            operation.removePreference(minBattery);
        }
    }
}
