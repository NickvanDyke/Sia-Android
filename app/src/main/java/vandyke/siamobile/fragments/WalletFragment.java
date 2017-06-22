package vandyke.siamobile.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
    private BigDecimal balanceUsd;
    private TextView balanceText;
    private TextView balanceUsdText;
    private TextView balanceUnconfirmedText;
    private ArrayList<Transaction> transactions;
    private NumberProgressBar syncBar;
    private TextView syncText;
    private TextView walletStatusText;
    private final ArrayList<TransactionExpandableGroup> transactionExpandableGroups = new ArrayList<>();
    private RecyclerView transactionList;
    private FrameLayout sendFrame;
    private FrameLayout receiveFrame;
    private FrameLayout unlockFrame;
    private WalletUnlockFragment unlockFrag;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);
        MainActivity.instance.getSupportActionBar().setTitle("Wallet");
        setHasOptionsMenu(true);

        final Button receiveButton = (Button)v.findViewById(R.id.receiveButton);
        final Button sendButton = (Button)v.findViewById(R.id.sendButton);

        if (MainActivity.theme == MainActivity.Theme.AMOLED || MainActivity.theme == MainActivity.Theme.CUSTOM) {
            v.findViewById(R.id.top_shadow).setVisibility(View.GONE);
        } else if (MainActivity.theme == MainActivity.Theme.DARK) {
            v.findViewById(R.id.top_shadow).setBackgroundResource(R.drawable.top_shadow_dark);
        }
        if (MainActivity.theme == MainActivity.Theme.AMOLED) {
            receiveButton.setBackgroundColor(android.R.color.transparent);
            sendButton.setBackgroundColor(android.R.color.transparent);
        }

        balanceHastings = new BigDecimal("0");
        balanceUsd = new BigDecimal("0");
        balanceText = (TextView)v.findViewById(R.id.balanceText);
        balanceUsdText = (TextView)v.findViewById(R.id.balanceUsdText);
        balanceUnconfirmedText = (TextView)v.findViewById(R.id.balanceUnconfirmed);
        transactions = new ArrayList<>();

        syncBar = (NumberProgressBar)v.findViewById(R.id.syncBar);
        syncText = (TextView)v.findViewById(R.id.syncText);
        syncBar.setProgressTextColor(MainActivity.defaultTextColor);
        walletStatusText = (TextView)v.findViewById(R.id.walletStatusText);

        transactionList = (RecyclerView)v.findViewById(R.id.transactionList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.instance);
        transactionList.setLayoutManager(layoutManager);
        transactionList.addItemDecoration(new DividerItemDecoration(transactionList.getContext(), layoutManager.getOrientation()));

        sendFrame = (FrameLayout)v.findViewById(R.id.sendFrame);
        receiveFrame = (FrameLayout)v.findViewById(R.id.receiveFrame);
        unlockFrame = (FrameLayout)v.findViewById(R.id.unlockFrame);

        final WalletSendFragment sendFrag = new WalletSendFragment();
        getFragmentManager().beginTransaction().add(R.id.sendFrame, sendFrag).commit();
        final WalletReceiveFragment recvFrag = new WalletReceiveFragment();
        getFragmentManager().beginTransaction().add(R.id.receiveFrame, recvFrag).commit();
        unlockFrag = new WalletUnlockFragment();
        getFragmentManager().beginTransaction().add(R.id.unlockFrame, unlockFrag).commit();

        refresh();

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sendFrame.getVisibility() == View.GONE) {
                    sendFrag.clearFields();
                    sendFrame.setVisibility(View.VISIBLE);
                } else {
                    sendFrame.setVisibility(View.GONE);
                    MainActivity.hideSoftKeyboard(getActivity());
                }
            }
        });
        receiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (receiveFrame.getVisibility() == View.GONE) {
                    recvFrag.getNewAddress();
                    receiveFrame.setVisibility(View.VISIBLE);
                } else {
                    receiveFrame.setVisibility(View.GONE);
                }
            }
        });
        balanceText.setOnClickListener(new View.OnClickListener() {
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
                unlockFrag.clearFields();
                unlockFrame.setVisibility(View.VISIBLE);
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
                    balanceText.setText(Wallet.round(Wallet.hastingsToSC(balanceHastings)));
                    BigDecimal netUnconfirmed = new BigDecimal(response.getString("unconfirmedincomingsiacoins"))
                            .subtract(new BigDecimal(response.getString("unconfirmedoutgoingsiacoins")));
                    balanceUnconfirmedText.setText(netUnconfirmed.compareTo(BigDecimal.ZERO) > 0 ? "+" : "" +
                            Wallet.round(Wallet.hastingsToSC(netUnconfirmed)) + " unconfirmed");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onError(SiaRequest.Error error) {
                super.onError(error);
                balanceText.setText("Loading...");
                walletStatusText.setText("Wallet Status:\nLoading...");
            }
        });

        Wallet.coincapSC(new Response.Listener() {
            public void onResponse(Object response) {
                try {
                    JSONObject json = new JSONObject((String) response);
                    double usdPrice = json.getDouble("usdPrice");
                    balanceUsd = Wallet.scToUsd(usdPrice, balanceHastings);
                    balanceUsdText.setText(Wallet.round(balanceUsd));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.instance, "Error retrieving USD value", Toast.LENGTH_SHORT).show();
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
