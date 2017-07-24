/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;
import vandyke.siamobile.R;
import vandyke.siamobile.SiaMobileApplication;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.backend.wallet.WalletMonitorService;
import vandyke.siamobile.misc.Utils;

public class WalletCreateFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_create, null);
        final CheckBox createFromSeed = (CheckBox) view.findViewById(R.id.walletCreateFromSeed);
        final EditText seedField = (EditText) view.findViewById(R.id.walletCreateSeed);
        seedField.setVisibility(View.GONE);
        createFromSeed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (createFromSeed.isChecked())
                    seedField.setVisibility(View.VISIBLE);
                else
                    seedField.setVisibility(View.GONE);
            }
        });

        final CheckBox forceCheck = (CheckBox) view.findViewById(R.id.walletCreateForce);
        final TextView forceWarning = (TextView) view.findViewById(R.id.walletCreateForceWarning);
        forceWarning.setVisibility(View.GONE);
        forceCheck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (forceCheck.isChecked())
                    forceWarning.setVisibility(View.VISIBLE);
                else
                    forceWarning.setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.walletCreateButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String password = ((EditText) view.findViewById(R.id.newPasswordCreate)).getText().toString();
                if (!password.equals(((EditText) view.findViewById(R.id.confirmNewPasswordCreate)).getText().toString())) {
                    Utils.snackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT);
                    return;
                }
                boolean force = forceCheck.isChecked();
                String dictionary = "english";
                if (createFromSeed.isChecked())
                    Wallet.initSeed(password, force, dictionary, seedField.getText().toString(), new SiaRequest.VolleyCallback() {
                        public void onSuccess(JSONObject response) {
                            Utils.successSnackbar(view);
                            container.setVisibility(View.GONE);
                            Utils.hideSoftKeyboard(getActivity());
                            WalletMonitorService.staticRefresh();
                            if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("cold_storage"))
                                showDialog();
                        }
                        public void onError(SiaRequest.Error error) {
                            if (error.getReason() == SiaRequest.Error.Reason.ANOTHER_WALLET_SCAN_UNDERWAY) {
                                Utils.snackbar(view, "Success. Scanning the blockchain for coins belonging to the given seed. Please wait - it can take a while", Snackbar.LENGTH_LONG);
                                container.setVisibility(View.GONE);
                                Utils.hideSoftKeyboard(getActivity());
                                WalletMonitorService.staticRefresh();
                            } else {
                                error.snackbar(view);
                            }
                        }
                    });
                else
                    Wallet.init(password, force, dictionary, new SiaRequest.VolleyCallback() {
                        public void onSuccess(JSONObject response) {
                            Utils.successSnackbar(view);
                            container.setVisibility(View.GONE);
                            Utils.hideSoftKeyboard(getActivity());
                            WalletMonitorService.staticRefresh();
                            if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("cold_storage"))
                                showDialog();
                        }
                        public void onError(SiaRequest.Error error) {
                            error.snackbar(view);
                        }
                    });
            }
        });

        view.findViewById(R.id.walletCreateCancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                container.setVisibility(View.GONE);
                Utils.hideSoftKeyboard(getActivity());
            }
        });
        return view;
    }

    private void showDialog() {
        Utils.getDialogBuilder(getActivity())
                .setTitle("IMPORTANT")
                .setMessage("You just created a wallet while in cold storage mode. While in cold storage mode," +
                        " Sia Mobile is not connected to the Sia network and does not have a copy of the Sia blockchain. This means it cannot show your correct balance or transactions." +
                        " You can send coins to any of the addresses of your cold storage wallet, and at any time in the future, load your wallet seed" +
                        " on a full node (such as Sia-UI on your computer or Sia Mobile's full node mode), and have access to your previously sent coins.")
                .setPositiveButton("I have read and understood this", null)
                .show();
    }
}
