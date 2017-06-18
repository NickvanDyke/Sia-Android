package vandyke.siamobile.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

public class WalletUnlockFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_unlock, null);
        view.findViewById(R.id.walletUnlockConfirm).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Wallet.unlock(((TextView)view.findViewById(R.id.walletPassword)).getText().toString(), new SiaRequest.VolleyCallback() {
                    public void onSuccess(JSONObject response) {
                        super.onSuccess(response);
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

}
