package vandyke.sia.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.android.volley.Request;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.sia.R;
import vandyke.sia.SiaRequest;

public class WalletFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);

        new SiaRequest(Request.Method.GET, "/wallet", new SiaRequest.VolleyCallback() {
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    System.out.println(jsonObject);
                    if (jsonObject.getString("unlocked").equals("false"))
                        unlockWallet();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return v;
    }

    public void unlockWallet() {
        final EditText editText = new EditText(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Wallet Password");
        builder.setView(editText);
        builder.setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SiaRequest request = new SiaRequest(Request.Method.POST, "/wallet/unlock", new SiaRequest.VolleyCallback() {
                    public void onResponse(String jsonObject) {
                        System.out.println(jsonObject);
                    }
                });
                request.addParam("encryptionpassword", editText.getText().toString());
                request.send();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
