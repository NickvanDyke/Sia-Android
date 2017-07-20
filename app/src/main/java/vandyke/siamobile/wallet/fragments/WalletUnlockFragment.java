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
import vandyke.siamobile.backend.wallet.WalletMonitorService;
import vandyke.siamobile.misc.Utils;

public class WalletUnlockFragment extends Fragment {

    private EditText password;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_unlock, null);
        password = (EditText)view.findViewById(R.id.walletPassword);
        view.findViewById(R.id.walletUnlockConfirm).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Wallet.unlock(password.getText().toString(), new SiaRequest.VolleyCallback() {
                    public void onSuccess(JSONObject response) {
                        Utils.successSnackbar(view);
                        Utils.hideSoftKeyboard(getActivity());
                        container.setVisibility(View.GONE);
                        WalletMonitorService.staticRefreshAll();
                    }
                    public void onError(SiaRequest.Error error) {
                        if (error.getReason() == SiaRequest.Error.Reason.ANOTHER_WALLET_SCAN_UNDERWAY) {
                            Utils.snackbar(view, error.getMsg(), Snackbar.LENGTH_LONG);
                            Utils.hideSoftKeyboard(getActivity());
                            container.setVisibility(View.GONE);
                        } else {
                            error.snackbar(view);
                        }
                    }
                });
            }
        });
        view.findViewById(R.id.walletUnlockCancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                container.setVisibility(View.GONE);
                Utils.hideSoftKeyboard(getActivity());
            }
        });

        return view;
    }
}
