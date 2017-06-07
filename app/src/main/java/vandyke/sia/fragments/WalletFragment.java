package vandyke.sia.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.sia.R;
import vandyke.sia.SiaRequest;
import vandyke.sia.api.Wallet;
import vandyke.sia.dialogs.ReceiveDialog;
import vandyke.sia.dialogs.SendDialog;
import vandyke.sia.dialogs.UnlockWalletDialog;
import vandyke.sia.transaction.Transaction;
import vandyke.sia.transaction.TransactionListAdapter;

import java.math.BigDecimal;
import java.util.ArrayList;

public class WalletFragment extends Fragment {

    private BigDecimal balanceHastings;

    private TextView balance;
    private ArrayList<Transaction> transactions;

    private TransactionListAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);
        setHasOptionsMenu(true);
        balance = (TextView)v.findViewById(R.id.balanceText);
        transactions = new ArrayList<>();


        refresh();

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
                builder.setMessage(Wallet.hastingsToSC(balanceHastings).toPlainString() + " Siacoins");
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        ListView transactionList = (ListView)v.findViewById(R.id.transactionList);
        adapter = new TransactionListAdapter(getContext(), R.layout.transaction_list_item, transactions);
        transactionList.setAdapter(adapter);

        Wallet.wallet(new SiaRequest.VolleyCallback() {
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

        return v;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionRefresh:
                refresh();
                break;
            case R.id.actionLock:
                Wallet.lock(new SiaRequest.VolleyCallback() {
                    public void onSuccess(JSONObject response) {
                        Toast.makeText(getContext(), "Wallet Locked", Toast.LENGTH_SHORT).show();
                    }
                });
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        // refresh balance
        Wallet.wallet(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                try {
                    balanceHastings = new BigDecimal(response.getString("confirmedsiacoinbalance"));
                    balance.setText(Wallet.hastingsToSC(balanceHastings).setScale(2, BigDecimal.ROUND_FLOOR).toPlainString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        // refresh transactions
        Wallet.transactions(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                transactions.clear();
                transactions.addAll(Transaction.populateTransactions(response));
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu);
    }
}
