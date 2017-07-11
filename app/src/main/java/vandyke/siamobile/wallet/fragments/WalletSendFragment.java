/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.SiaMobileApplication;
import vandyke.siamobile.misc.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WalletSendFragment extends Fragment {

    private EditText recipient;
    private EditText amount;
    private TextView feeText;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_send, null);
        recipient = (EditText) view.findViewById(R.id.sendRecipient);
        amount = (EditText)view.findViewById(R.id.sendAmount);
        feeText = (TextView)view.findViewById(R.id.walletSendFee);
        if (!SiaMobileApplication.prefs.getBoolean("feesEnabled", false))
            feeText.setVisibility(View.GONE);
        amount.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (amount.getText().toString().equals(""))
                    feeText.setText("0.5% App fee: 0.000");
                else
                    feeText.setText("0.5% App fee: " + new BigDecimal(s.toString()).multiply(Utils.devFee).setScale(3, RoundingMode.FLOOR).toPlainString() + " SC");
            }
            public void afterTextChanged(Editable s) {
            }
        });
        view.findViewById(R.id.walletChange).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BigDecimal sendAmount = Wallet.scToHastings(amount.getText().toString());
                if (SiaMobileApplication.prefs.getBoolean("feesEnabled", false))
                    Wallet.sendSiacoinsWithDevFee(sendAmount,
                            recipient.getText().toString(),
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
                else
                    Wallet.sendSiacoins(sendAmount,
                            ((EditText) view.findViewById(R.id.sendRecipient)).getText().toString(),
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
            public void onClick(View v) {
                container.setVisibility(View.GONE);
                Utils.hideSoftKeyboard(getActivity());
            }
        });

        return view;
    }
}
