package vandyke.sia.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.volley.Request;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.sia.R;
import vandyke.sia.SiaRequest;
import vandyke.sia.dialogs.ReceiveDialog;
import vandyke.sia.dialogs.SendDialog;
import vandyke.sia.dialogs.UnlockWalletDialog;

public class WalletFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);

        SiaRequest walletRequest = new SiaRequest(Request.Method.GET, "/wallet", new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                try {
                    System.out.println(response);
                    if (response.getString("unlocked").equals("false"))
                        UnlockWalletDialog.createAndShow(getFragmentManager());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        walletRequest.send();

        final Button receiveButton = (Button)v.findViewById(R.id.receiveButton);
        receiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ReceiveDialog.createAndShow(getFragmentManager());
            }
        });

        final Button sendButton = (Button)v.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SendDialog.createAndShow(getFragmentManager());
            }
        });

        return v;
    }
}
