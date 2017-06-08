package vandyke.siamobile.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.R;
import vandyke.siamobile.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.dialogs.WalletChangePasswordDialog;
import vandyke.siamobile.dialogs.WalletReceiveDialog;
import vandyke.siamobile.dialogs.WalletSendDialog;
import vandyke.siamobile.dialogs.WalletUnlockDialog;
import vandyke.siamobile.transaction.Transaction;
import vandyke.siamobile.transaction.TransactionListAdapter;

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
                WalletReceiveDialog.createAndShow(getFragmentManager());
            }
        });

        final Button sendButton = (Button)v.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                WalletSendDialog.createAndShow(getFragmentManager());
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

//        Wallet.wallet(new SiaRequest.VolleyCallback() {
//            public void onSuccess(JSONObject response) {
//                try {
//                    System.out.println(response);
//                    if (response.getString("unlocked").equals("false"))
//                        WalletUnlockDialog.createAndShow(getFragmentManager());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            public void onError(SiaRequest.Error error) {
//                error.toast();
//            }
//        });

        return v;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionRefresh:
                refresh();
                break;
            case R.id.actionUnlock:
                WalletUnlockDialog.createAndShow(getFragmentManager());
                break;
            case R.id.actionLock:
                Wallet.lock(new SiaRequest.VolleyCallback());
                break;
            case R.id.actionChangePassword:
                WalletChangePasswordDialog.createAndShow(getFragmentManager());
                break;
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
        //TODO: figure out a GOOD way to Toast "Refreshed" if both requests complete successfully
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu);
    }
}
