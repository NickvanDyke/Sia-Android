package vandyke.siamobile.fragments.wallet;

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
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

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
                    MainActivity.snackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT);
                    return;
                }
                boolean force = forceCheck.isChecked();
                String dictionary = "english";
                if (createFromSeed.isChecked())
                    Wallet.initSeed(password, force, dictionary, seedField.getText().toString(), new SiaRequest.VolleyCallback(view) {
                        public void onSuccess(JSONObject response) {
                            super.onSuccess(response);
                            container.setVisibility(View.GONE);
                            MainActivity.hideSoftKeyboard(getActivity());
//                                    WalletFragment.refreshWallet(getFragmentManager());
                        }
                    });
                else
                    Wallet.init(password, force, dictionary, new SiaRequest.VolleyCallback(view) {
                        public void onSuccess(JSONObject response) {
                            super.onSuccess(response);
                            container.setVisibility(View.GONE);
                            MainActivity.hideSoftKeyboard(getActivity());
//                                    WalletFragment.refreshWallet(getFragmentManager());
                        }
                    });
            }
        });

        view.findViewById(R.id.walletCreateCancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                container.setVisibility(View.GONE);
                MainActivity.hideSoftKeyboard(getActivity());
            }
        });
        return view;
    }
}
