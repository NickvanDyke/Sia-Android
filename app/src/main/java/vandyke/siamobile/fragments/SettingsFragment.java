package vandyke.siamobile.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import fi.iki.elonen.NanoHTTPD;
import vandyke.siamobile.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    private EditTextPreference remoteAddress;
    private EditTextPreference apiPass;

    private static final int SELECT_PICTURE = 1;

    public void onCreate(Bundle savedInstanceState) { // TODO: restarts app on first loading
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        final ListPreference operationMode = (ListPreference) findPreference("operationMode");
        remoteAddress = (EditTextPreference) findPreference("remoteAddress");
        apiPass = (EditTextPreference) findPreference("apiPass");
        setRemoteSettingsVisibility();

        final EditTextPreference decimal = ((EditTextPreference) findPreference("displayedDecimalPrecision"));

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

        final SwitchPreference useExternal = (SwitchPreference) findPreference("useExternal");
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

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                switch (key) {
                    case "operationMode":
                        setRemoteSettingsVisibility();
                        if (sharedPreferences.getString("operationMode", "cold_storage").equals("remote_full_node")) {
                            editor.putString("address", sharedPreferences.getString("remoteAddress", "192.168.1.11:9980"));
                            ColdStorageWallet.destroy();
                            Siad.stopSiad(getActivity());
                        } else if (sharedPreferences.getString("operationMode", "cold_storage").equals("local_full_node")) {
                            editor.putString("address", "localhost:9980");
                            ColdStorageWallet.destroy();
                            Siad.getInstance(getActivity()).start(getActivity());
                        } else if (sharedPreferences.getString("operationMode", "cold_storage").equals("cold_storage")) {
                            editor.putString("address", "localhost:9980");
                            Siad.stopSiad(getActivity());
                            try {
                                ColdStorageWallet.getInstance(getActivity()).start(NanoHTTPD.SOCKET_READ_TIMEOUT);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        editor.apply();
//                        WalletFragment.refreshWallet(getFragmentManager());
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
        MainActivity.prefs.registerOnSharedPreferenceChangeListener(prefsListener);
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
        } else {
            remoteAddress.setEnabled(false);
            apiPass.setEnabled(false);
        }
    }
}
