package vandyke.siamobile.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WalletSendFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_send, null);
        final EditText amount = (EditText)view.findViewById(R.id.sendAmount);
        final TextView feeText = (TextView)view.findViewById(R.id.walletSendFee);
        if (MainActivity.theme == MainActivity.Theme.CUSTOM)
            feeText.setTextColor(Color.GRAY);
        if (!MainActivity.prefs.getBoolean("feesEnabled", true))
            feeText.setVisibility(View.GONE);
        amount.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (amount.getText().toString().equals(""))
                    feeText.setText("0.5% App fee: 0.000");
                else
                    feeText.setText("0.5% App fee: " + new BigDecimal(s.toString()).multiply(MainActivity.devFee).setScale(3, RoundingMode.FLOOR).toPlainString() + " SC");
            }
            public void afterTextChanged(Editable s) {
            }
        });
        view.findViewById(R.id.walletSend).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BigDecimal sendAmount = Wallet.scToHastings(amount.getText().toString());
                if (MainActivity.prefs.getBoolean("feesEnabled", true))
                    Wallet.sendSiacoinsWithDevFee(sendAmount,
                            ((EditText) view.findViewById(R.id.sendRecipient)).getText().toString(),
                            new SiaRequest.VolleyCallback() {
                                public void onSuccess(JSONObject response) {
                                    super.onSuccess(response);
                                    container.setVisibility(View.GONE);
                                    MainActivity.hideSoftKeyboard(getActivity());
                                }
                            });
                else
                    Wallet.sendSiacoins(sendAmount,
                            ((EditText) view.findViewById(R.id.sendRecipient)).getText().toString(),
                            new SiaRequest.VolleyCallback());
            }
        });
        view.findViewById(R.id.walletSendCancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                container.setVisibility(View.GONE);
                MainActivity.hideSoftKeyboard(getActivity());
            }
        });

        return view;
    }
}
