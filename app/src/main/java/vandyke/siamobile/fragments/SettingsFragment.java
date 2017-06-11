package vandyke.siamobile.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;



    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
        final ListPreference operationMode = (ListPreference)findPreference("operationMode");
        final EditTextPreference remoteAddress = (EditTextPreference)findPreference("remoteAddress");
        final EditTextPreference apiPass = (EditTextPreference)findPreference("apiPass");
        setRemoteSettingsVisibility(remoteAddress, apiPass);

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key) {
                    case "operationMode":
                        setRemoteSettingsVisibility(remoteAddress, apiPass);
                        break;
                }
            }
        };
        MainActivity.prefs.registerOnSharedPreferenceChangeListener(prefsListener);
    }

    private void setRemoteSettingsVisibility(Preference p1, Preference p2) {
        if (MainActivity.prefs.getString("operationMode", "remote_full_node").equals("remote_full_node")) {
            p1.setVisible(true);
            p2.setVisible(true);
        } else if (MainActivity.prefs.getString("operationMode", "remote_full_node").equals("local_wallet_and_server")) {
            p1.setVisible(false);
            p2.setVisible(false);
        }
    }
}
