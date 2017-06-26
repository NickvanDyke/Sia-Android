package vandyke.siamobile.fragments;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

public class WalletReceiveFragment extends Fragment {

    private TextView address;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_receive, null);
        address = (TextView)view.findViewById(R.id.receiveAddress);
        view.findViewById(R.id.walletAddressCopy).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("receive address", ((TextView)view.findViewById(R.id.receiveAddress)).getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), "Copied receive address", Toast.LENGTH_SHORT).show();
                container.setVisibility(View.GONE);
            }
        });
        view.findViewById(R.id.walletAddressClose).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                container.setVisibility(View.GONE);
            }
        });
        return view;
    }

    public void getNewAddress() {
        Wallet.newAddress(new SiaRequest.VolleyCallback(getActivity()) {
            public void onSuccess(JSONObject response) {
                try {
                    address.setText(response.getString("address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
