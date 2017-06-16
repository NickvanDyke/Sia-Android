package vandyke.siamobile.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.instance.getSupportActionBar().setTitle("Settings");
        addPreferencesFromResource(R.xml.settings);
        final ListPreference operationMode = (ListPreference)findPreference("operationMode");
        final EditTextPreference remoteAddress = (EditTextPreference)findPreference("remoteAddress");
        final EditTextPreference apiPass = (EditTextPreference)findPreference("apiPass");
        setRemoteSettingsVisibility(remoteAddress, apiPass);

        final EditTextPreference decimal = ((EditTextPreference)findPreference("displayedDecimalPrecision"));

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key) {
                    case "operationMode":
                        setRemoteSettingsVisibility(remoteAddress, apiPass);
                        break;
                    case "darkModeEnabled":
                        break;
                }
            }
        };
        MainActivity.prefs.registerOnSharedPreferenceChangeListener(prefsListener);
    }

    private void setRemoteSettingsVisibility(Preference p1, Preference p2) {
        if (MainActivity.prefs.getString("operationMode", "remote_full_node").equals("remote_full_node")) {
            p1.setEnabled(true);
            p2.setEnabled(true);
        } else if (MainActivity.prefs.getString("operationMode", "remote_full_node").equals("local_wallet_and_server")) {
            p1.setEnabled(false);
            p2.setEnabled(false);
        }
    }
}
