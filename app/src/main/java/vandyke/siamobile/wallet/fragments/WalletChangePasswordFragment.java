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
import android.widget.EditText;
import org.json.JSONObject;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.misc.Utils;

public class WalletChangePasswordFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_change_password, null);
        view.findViewById(R.id.walletChange).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String newPassword = ((EditText)view.findViewById(R.id.newPassword)).getText().toString();
                if (!newPassword.equals(((EditText)view.findViewById(R.id.confirmNewPassword)).getText().toString())) {
                    Utils.snackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT);
                    return;
                }
                Wallet.changePassword(((EditText) view.findViewById(R.id.currentPassword)).getText().toString(),
                        newPassword, new SiaRequest.VolleyCallback() {
                            public void onSuccess(JSONObject response) {
                                Utils.successSnackbar(view);
                                container.setVisibility(View.GONE);
                                Utils.hideSoftKeyboard(getActivity());
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
}
