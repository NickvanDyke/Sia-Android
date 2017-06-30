package vandyke.siamobile.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.Toast;
import vandyke.siamobile.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

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
                if (((String)o).equals("local_full_node")
                        && !(MainActivity.abi.equals("aarch64") || MainActivity.abi.equals("x86_64"))) {
                    Toast.makeText(getActivity(), "Sorry, but your device's CPU architecture is not supported by siad. There is nothing Sia Mobile can do about this", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

        final SwitchPreference useExternal = (SwitchPreference)findPreference("useExternal");
        useExternal.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
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
                        if (sharedPreferences.getString("operationMode", "cold_storage").equals("remote_full_node")) {
                            editor.putString("address", sharedPreferences.getString("remoteAddress", "192.168.1.11:9980"));
                            Siad.getInstance(getActivity()).stop();
                        } else if (sharedPreferences.getString("operationMode", "cold_storage").equals("local_full_node")) {
                            editor.putString("address", "localhost:9980");
                            Siad.getInstance(getActivity()).start();
                        } else if (sharedPreferences.getString("operationMode", "cold_storage").equals("cold_storage")) {
                            editor.putString("address", "localhost:9980");
                            LocalWallet.getInstance().startListening(9980);
                        }
                        editor.apply();
                        refreshWallet();
                        break;
                    case "remoteAddress":
                        if (sharedPreferences.getString("operationMode", "cold_storage").equals("remote_full_node")) {
                            editor.putString("address", sharedPreferences.getString("remoteAddress", "192.168.1.11:9980"));
                            editor.apply();
                        }
                        refreshWallet();
                        break;
                    case "apiPass":
                    case "hideZero":
                    case "displayedDecimalPrecision":
                        refreshWallet();
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
                SharedPreferences.Editor prefsEditor = MainActivity.prefs.edit();
                prefsEditor.putString("customBgBase64", Base64.encodeToString(b, Base64.DEFAULT));
                prefsEditor.apply();
            }
        }
    }

    private void setRemoteSettingsVisibility() {
        if (MainActivity.prefs.getString("operationMode", "cold_storage").equals("remote_full_node")) {
            remoteAddress.setEnabled(true);
            apiPass.setEnabled(true);
        } else if (MainActivity.prefs.getString("operationMode", "cold_storage").equals("local_full_node")) {
            remoteAddress.setEnabled(false);
            apiPass.setEnabled(false);
        }
    }

    private void refreshWallet() {
        WalletFragment fragment = (WalletFragment)getFragmentManager().findFragmentByTag("WalletFragment");
        if (fragment != null)
            fragment.refresh();
    }
}
