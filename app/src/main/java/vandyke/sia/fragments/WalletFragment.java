package vandyke.sia.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.sia.R;
import vandyke.sia.SiaRequest;
import vandyke.sia.dialogs.ReceiveDialog;
import vandyke.sia.dialogs.SendDialog;
import vandyke.sia.dialogs.UnlockWalletDialog;

import java.math.BigDecimal;

public class WalletFragment extends Fragment {

    private BigDecimal balanceHastings;

    private TextView balance;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);
        setHasOptionsMenu(true);
        balance = (TextView)v.findViewById(R.id.balanceText);
        refreshBalance();

        SiaRequest.wallet(new SiaRequest.VolleyCallback() {
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

        balance.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Exact Balance");
                builder.setMessage(SiaRequest.hastingsToSC(balanceHastings).toPlainString() + " Siacoins");
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        return v;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        switch (item.getItemId()) {
            case R.id.actionRefresh:
                refreshBalance();
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    public void refreshBalance() {
        SiaRequest.wallet(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                try {
                    balanceHastings = new BigDecimal(response.getString("confirmedsiacoinbalance"));
                    balance.setText(SiaRequest.hastingsToSC(balanceHastings)
                                    .setScale(2, BigDecimal.ROUND_FLOOR).toPlainString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu);
    }
}
