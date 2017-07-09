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
import android.widget.Button;
import android.widget.EditText;
import org.json.JSONObject;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.misc.Utils;

public class WalletSweepSeedFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_sweep, null);
        ((Button)view.findViewById(R.id.walletAddSeed)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Wallet.sweepSeed("english", ((EditText)view.findViewById(R.id.walletSweepSeed)).getText().toString(),
                        new SiaRequest.VolleyCallback(view) {
                            public void onSuccess(JSONObject response) {
                                super.onSuccess(response);
                                container.setVisibility(View.GONE);
                                Utils.hideSoftKeyboard(getActivity());
                            }
                        });
            }
        });
        ((Button)view.findViewById(R.id.walletCreateCancel)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                container.setVisibility(View.GONE);
                Utils.hideSoftKeyboard(getActivity());
            }
        });
        return view;
    }
}
