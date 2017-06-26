package vandyke.siamobile.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

public class WalletUnlockFragment extends Fragment {

    private EditText password;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_unlock, null);
        password = (EditText)view.findViewById(R.id.walletPassword);
        view.findViewById(R.id.walletUnlockConfirm).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Wallet.unlock(password.getText().toString(), new SiaRequest.VolleyCallback(getActivity()) {
                    public void onSuccess(JSONObject response) {
                        super.onSuccess(response);
                        WalletFragment.staticRefresh();
                        container.setVisibility(View.GONE);
                        MainActivity.hideSoftKeyboard(getActivity());
                    }
                });
            }
        });
        view.findViewById(R.id.walletUnlockCancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                container.setVisibility(View.GONE);
                MainActivity.hideSoftKeyboard(getActivity());
            }
        });

        return view;
    }

    public void clearFields() {
        password.setText("");
    }

}
