package vandyke.siamobile.fragments.wallet;

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

public class WalletAddSeedFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_add_seed, null);
        view.findViewById(R.id.walletCreateButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Wallet.seed(((EditText)view.findViewById(R.id.walletPassword)).getText().toString(), "english",
                        ((EditText)view.findViewById(R.id.walletAddSeed)).getText().toString(),
                        new SiaRequest.VolleyCallback(view) {
                            public void onSuccess(JSONObject response) {
                                super.onSuccess(response);
                                container.setVisibility(View.GONE);
                                MainActivity.hideSoftKeyboard(getActivity());
                            }
                        });
            }
        });
        view.findViewById(R.id.walletCreateCancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                container.setVisibility(View.GONE);
                MainActivity.hideSoftKeyboard(getActivity());
            }
        });
        return view;
    }
}
