package vandyke.siamobile.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import vandyke.siamobile.BuildConfig;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.Siad;

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    private EditTextPreference remoteAddress;
    private EditTextPreference apiPass;

    private static final int SELECT_PICTURE = 1;

    public void onCreate(Bundle savedInstanceState) { // TODO: restarts app on first loading
        super.onCreate(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Settings");
        addPreferencesFromResource(R.xml.settings);

        final ListPreference operationMode = (ListPreference)findPreference("operationMode");
        remoteAddress = (EditTextPreference)findPreference("remoteAddress");
        apiPass = (EditTextPreference)findPreference("apiPass");
        setRemoteSettingsVisibility();

        final EditTextPreference decimal = ((EditTextPreference)findPreference("displayedDecimalPrecision"));

        operationMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (MainActivity.abi.equals("aarch64") || MainActivity.abi.equals("x86_64"))
                    return true;
                else
                    Toast.makeText(getActivity(), "Sorry, but your device's CPU architecture is not supported by siad. There is nothing Sia Mobile can do about this", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        final SwitchPreference useExternal = (SwitchPreference)findPreference("useExternal");
        useExternal.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                System.out.println( MainActivity.externalStorageStateDescription());
                if (MainActivity.isExternalStorageWritable())
                    return true;
                else
                    Toast.makeText(getActivity(), "Error: " + MainActivity.externalStorageStateDescription(), Toast.LENGTH_LONG).show();
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

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                switch (key) {
                    case "operationMode":
                        setRemoteSettingsVisibility();
                        if (sharedPreferences.getString("operationMode", "remote_full_node").equals("remote_full_node")) {
                            editor.putString("address", sharedPreferences.getString("remoteAddress", "192.168.1.11:9980"));
                            Siad.getInstance(getActivity()).stop();
                        } else if (sharedPreferences.getString("operationMode", "remote_full_node").equals("local_full_node")) {
                            editor.putString("address", "localhost:9980");
                            Siad.getInstance(getActivity()).start();
                        }
                        editor.apply();
                        break;
                    case "remoteAddress":
                        if (sharedPreferences.getString("operationMode", "remote_full_node").equals("remote_full_node")) {
                            editor.putString("address", sharedPreferences.getString("remoteAddress", "192.168.1.11:9980"));
                            editor.apply();
                        }
                        break;
                    case "theme":// restart to apply the theme; don't need to change theme variable since app is restarting and it'll load it
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
