package vandyke.siamobile.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import com.daimajia.numberprogressbar.NumberProgressBar;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.Consensus;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.dialogs.*;
import vandyke.siamobile.transaction.Transaction;
import vandyke.siamobile.transactionslist.TransactionExpandableGroup;
import vandyke.siamobile.transactionslist.TransactionListAdapter;

import java.math.BigDecimal;
import java.util.ArrayList;

public class WalletFragment extends Fragment {

    private BigDecimal balanceHastings;
    private TextView balance;
    private TextView balanceUnconfirmed;

    private ArrayList<Transaction> transactions;

    private NumberProgressBar syncBar;
    private TextView syncText;

    private TextView walletStatusText;

    private final ArrayList<TransactionExpandableGroup> transactionExpandableGroups = new ArrayList<>();

    private RecyclerView transactionList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);
        setHasOptionsMenu(true);

        balance = (TextView)v.findViewById(R.id.balanceText);
        balanceUnconfirmed = (TextView)v.findViewById(R.id.balanceUnconfirmed);
        transactions = new ArrayList<>();

        syncBar = (NumberProgressBar)v.findViewById(R.id.syncBar);
        syncText = (TextView)v.findViewById(R.id.syncText);
        walletStatusText = (TextView)v.findViewById(R.id.walletStatusText);

        transactionList = (RecyclerView)v.findViewById(R.id.transactionList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.instance);
        transactionList.setLayoutManager(layoutManager);

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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.instance);
                builder.setTitle("Exact Balance");
                builder.setMessage(Wallet.hastingsToSC(balanceHastings).toPlainString() + " Siacoins");
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        return v;
    }

    private TransactionExpandableGroup transactionToGroupWithChild(Transaction tx) {
        ArrayList<Transaction> child = new ArrayList<>();
        child.add(tx);
        return new TransactionExpandableGroup(tx.getNetValueStringRounded(), tx.getConfirmationDate(), child);
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
            case R.id.actionViewSeeds:
                WalletSeedsDialog.createAndShow(getFragmentManager());
                break;
            case R.id.actionCreateWallet:
                WalletCreateDialog.createAndShow(getFragmentManager());
                break;
            case R.id.actionSweepSeed:
                WalletSweepSeedDialog.createAndShow(getFragmentManager());
                break;
            case R.id.actionViewAddresses:
                WalletAddressesDialog.createAndShow(getFragmentManager());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        refreshBalanceAndStatus();
        refreshTransactions();
        refreshSyncProgress();
        //TODO: figure out a GOOD way to Toast "Refreshed" if all requests complete successfully
        //TODO: auto refresh every x seconds. Eventually add option to refresh in background, with notifications?
    }

    public void refreshBalanceAndStatus() {
        Wallet.wallet(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getString("encrypted").equals("false"))
                        walletStatusText.setText("Wallet Status:\nNo Wallet");
                    else if (response.getString("unlocked").equals("false"))
                        walletStatusText.setText("Wallet Status:\nLocked");
                    else
                        walletStatusText.setText("Wallet Status:\nUnlocked");
                    balanceHastings = new BigDecimal(response.getString("confirmedsiacoinbalance"));
                    balance.setText(Wallet.round(Wallet.hastingsToSC(balanceHastings)));
                    BigDecimal netUnconfirmed = new BigDecimal(response.getString("unconfirmedincomingsiacoins"))
                            .subtract(new BigDecimal(response.getString("unconfirmedoutgoingsiacoins")));
                    balanceUnconfirmed.setText(Wallet.round(Wallet.hastingsToSC(netUnconfirmed)) + " unconfirmed");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onError(SiaRequest.Error error) {
                super.onError(error);
                balance.setText("Loading...");
                walletStatusText.setText("Wallet Status:\nLoading...");
            }
        });
    }

    public void refreshTransactions() {
        Wallet.transactions(new SiaRequest.VolleyCallback() {
        public void onSuccess(JSONObject response) {
            boolean hideZero = MainActivity.prefs.getBoolean("hideZero", false);
            transactions = Transaction.populateTransactions(response);
            transactionExpandableGroups.clear();
            for (Transaction tx : transactions) {
                if (hideZero && tx.isNetZero())
                    continue;
                transactionExpandableGroups.add(transactionToGroupWithChild(tx));
            }
            transactionList.setAdapter(new TransactionListAdapter(transactionExpandableGroups));
        }
    });
    }

    public void refreshSyncProgress() {
        Consensus.consensus(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getBoolean("synced")) {
                        syncText.setText("Synced");
                        syncBar.setProgress(100);
                    } else {
                        syncText.setText("Syncing");
                        syncBar.setProgress((int)(((double)response.getInt("height") / estimatedBlockHeightAt(System.currentTimeMillis() / 1000)) * 100));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onError(SiaRequest.Error error) {
                super.onError(error);
                syncText.setText("Not Synced");
                syncBar.setProgress(0);
            }
        });
    }

    // note time should be in seconds
    public int estimatedBlockHeightAt(long time) {
        long block100kTimestamp = 1492126789; // Unix timestamp; seconds
        int blockTime = 9; // overestimate
        long diff = time - block100kTimestamp;
        return (int)(100000 + (diff / 60 / blockTime));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu);
    }
}
