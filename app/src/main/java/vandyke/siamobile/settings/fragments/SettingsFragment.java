/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.settings.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.*;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import fi.iki.elonen.NanoHTTPD;
import vandyke.siamobile.BuildConfig;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.backend.ColdStorageWallet;
import vandyke.siamobile.backend.Siad;
import vandyke.siamobile.backend.SiadMonitor;
import vandyke.siamobile.misc.SiaMobileApplication;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    private PreferenceCategory operation;
    private EditTextPreference remoteAddress;
    private EditTextPreference apiPass;
    private SwitchPreference runLocalNodeInBackground;
    private SwitchPreference runLocalNodeOffWifi;
    private SwitchPreference useExternal;
    private EditTextPreference minBattery;

    private static final int SELECT_PICTURE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        operation = (PreferenceCategory)findPreference("operationCategory");
        remoteAddress = (EditTextPreference) findPreference("remoteAddress");
        apiPass = (EditTextPreference) findPreference("apiPass");
        runLocalNodeInBackground = (SwitchPreference)findPreference("runLocalNodeInBackground");
        runLocalNodeOffWifi = (SwitchPreference)findPreference("runLocalNodeOffWifi");
        useExternal = (SwitchPreference) findPreference("useExternal");
        minBattery = (EditTextPreference)findPreference("localNodeMinBattery");
        setRemoteSettingsVisibility();
        setLocalSettingsVisibility();

        final EditTextPreference decimal = ((EditTextPreference) findPreference("displayedDecimalPrecision"));

        final ListPreference operationMode = (ListPreference) findPreference("operationMode");
        operationMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (((String) o).equals("local_full_node")
                        && !(MainActivity.abi.equals("arm64"))) {
                    MainActivity.snackbar(getView(), "Sorry, but your device's CPU architecture is not supported by Sia's full node", Snackbar.LENGTH_LONG);
                    return false;
                }
                return true;
            }
        });

        useExternal.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (MainActivity.isExternalStorageWritable())
                    return true;
                else
                    MainActivity.snackbar(getView(), "Error: " + MainActivity.externalStorageStateDescription(), Snackbar.LENGTH_LONG);
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
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null)
            return;
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null)
            return;
        actionBar.setTitle("Settings");

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                switch (key) {
                    case "operationMode":
                        setRemoteSettingsVisibility();
                        setLocalSettingsVisibility();
                        if (sharedPreferences.getString("operationMode", "cold_storage").equals("remote_full_node")) {
                            editor.putString("address", sharedPreferences.getString("remoteAddress", "192.168.1.11:9980"));
                            ColdStorageWallet.destroy();
                            getActivity().stopService(new Intent(getActivity(), SiadMonitor.class));
                        } else if (sharedPreferences.getString("operationMode", "cold_storage").equals("local_full_node")) {
                            editor.putString("address", "localhost:9980");
                            ColdStorageWallet.destroy();
                            getActivity().startService(new Intent(getActivity(), SiadMonitor.class));
                        } else if (sharedPreferences.getString("operationMode", "cold_storage").equals("cold_storage")) {
                            editor.putString("address", "localhost:9990");
                            getActivity().stopService(new Intent(getActivity(), SiadMonitor.class));
                            try {
                                ColdStorageWallet.getInstance(getActivity()).start(NanoHTTPD.SOCKET_READ_TIMEOUT);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        editor.apply();
//                        WalletFragment.refreshWallet(getFragmentManager());
                        break;
                    case "runLocalNodeOffWifi":
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

                        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI
                                || SiaMobileApplication.prefs.getBoolean("runLocalNodeOffWifi", false)) {
                            getActivity().startService(new Intent(getActivity(), Siad.class));
                        } else {
                            getActivity().stopService(new Intent(getActivity(), Siad.class));
                        }
                        break;
                    case "localNodeMinBattery":
                        Intent batteryStatus = getActivity().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        if (level >= Integer.parseInt(SiaMobileApplication.prefs.getString("localNodeMinBattery", "20")))
                            getActivity().startService(new Intent(getActivity(), Siad.class));
                        else
                            getActivity().stopService(new Intent(getActivity(), Siad.class));
                        break;
                    case "remoteAddress":
                        if (sharedPreferences.getString("operationMode", "cold_storage").equals("remote_full_node")) {
                            editor.putString("address", sharedPreferences.getString("remoteAddress", "192.168.1.11:9980"));
                            editor.apply();
                        }
//                        WalletFragment.refreshWallet(getFragmentManager());
                        break;
                    case "apiPass":
                    case "hideZero":
                    case "displayedDecimalPrecision":
//                        WalletFragment.refreshWallet(getFragmentManager());
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
            operation.addPreference(runLocalNodeInBackground);
            operation.addPreference(runLocalNodeOffWifi);
            operation.addPreference(useExternal);
            operation.addPreference(minBattery);
        } else {
            operation.removePreference(runLocalNodeInBackground);
            operation.removePreference(runLocalNodeOffWifi);
            operation.removePreference(useExternal);
            operation.removePreference(minBattery);
        }
    }
}
