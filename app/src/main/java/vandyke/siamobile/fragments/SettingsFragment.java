package vandyke.siamobile.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.widget.Toast;
import vandyke.siamobile.BuildConfig;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    private EditTextPreference remoteAddress;
    private EditTextPreference apiPass;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.instance.getSupportActionBar().setTitle("Settings");
        addPreferencesFromResource(R.xml.settings);

        final ListPreference operationMode = (ListPreference)findPreference("operationMode");
        remoteAddress = (EditTextPreference)findPreference("remoteAddress");
        apiPass = (EditTextPreference)findPreference("apiPass");
        setRemoteSettingsVisibility();

        final EditTextPreference decimal = ((EditTextPreference)findPreference("displayedDecimalPrecision"));

        final SwitchPreference useExternal = (SwitchPreference)findPreference("useExternal");
        useExternal.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                System.out.println( MainActivity.externalStorageStateDescription());
                if (MainActivity.isExternalStorageWritable())
                    return true;
                else
                    Toast.makeText(MainActivity.instance, "Error: " + MainActivity.externalStorageStateDescription(), Toast.LENGTH_LONG).show();
                return false;
            }
        });

        final Preference openAppSettings = findPreference("openAppSettings");
//        clearInternal.setSummary("Sia Mobile is using " + MainActivity.getInternalFilesSize() + " bytes of internal storage and " +
//                MainActivity.getExternalFilesSize() + " bytes of external storage. Tap to open app settings page where you can clear these." +
//                "WARNING: Have your wallet seed stored somewhere first! Clearing Sia Mobile's data will delete your wallet files!");
        openAppSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent appSettings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                appSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(appSettings);
                return false;
            }
        });

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key) {
                    case "operationMode":
                        setRemoteSettingsVisibility();
                        break;
                }
            }
        };
        MainActivity.prefs.registerOnSharedPreferenceChangeListener(prefsListener);
    }

    private void setRemoteSettingsVisibility() {
        if (MainActivity.prefs.getString("operationMode", "remote_full_node").equals("remote_full_node")) {
            remoteAddress.setEnabled(true);
            apiPass.setEnabled(true);
        } else if (MainActivity.prefs.getString("operationMode", "remote_full_node").equals("local_full_node")) {
            remoteAddress.setEnabled(false);
            apiPass.setEnabled(false);
        }
    }
}
