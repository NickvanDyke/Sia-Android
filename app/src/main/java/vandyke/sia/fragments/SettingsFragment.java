package vandyke.sia.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import vandyke.sia.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }
}
