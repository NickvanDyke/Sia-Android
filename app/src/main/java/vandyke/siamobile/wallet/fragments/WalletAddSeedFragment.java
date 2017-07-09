/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import org.json.JSONObject;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.misc.Utils;

public class WalletAddSeedFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_add_seed, null);
        view.findViewById(R.id.walletCreateButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Wallet.seed(((EditText)view.findViewById(R.id.walletPassword)).getText().toString(), "english",
                        ((EditText)view.findViewById(R.id.walletAddSeed)).getText().toString(),
                        new SiaRequest.VolleyCallback() {
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
            public void onClick(View view) {
                container.setVisibility(View.GONE);
                Utils.hideSoftKeyboard(getActivity());
            }
        });
        return view;
    }
}
