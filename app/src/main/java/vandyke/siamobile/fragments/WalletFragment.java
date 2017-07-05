package vandyke.siamobile.fragments;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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

    public static int SYNC_NOTIFICATION = 0;
    private Handler handler;
    private Runnable refreshTask;

    private BigDecimal balanceHastings;
    private BigDecimal balanceHastingsUnconfirmed;
    private BigDecimal balanceUsd;
    private TextView balanceText;
    private TextView balanceUsdText;
    private TextView balanceUnconfirmedText;
    private ArrayList<Transaction> transactions;
    private NumberProgressBar syncBar;
    private TextView syncText;
    private TextView walletStatusText;
    private RecyclerView transactionList;
    private final ArrayList<TransactionExpandableGroup> transactionExpandableGroups = new ArrayList<>();
    private FrameLayout sendFrame;
    private FrameLayout receiveFrame;
    private FrameLayout unlockFrame;
    private WalletUnlockFragment unlockFrag;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);
        setHasOptionsMenu(true);

        final Button receiveButton = (Button) v.findViewById(R.id.receiveButton);
        final Button sendButton = (Button) v.findViewById(R.id.sendButton);

        if (MainActivity.theme == MainActivity.Theme.AMOLED || MainActivity.theme == MainActivity.Theme.CUSTOM) {
            v.findViewById(R.id.top_shadow).setVisibility(View.GONE);
        } else if (MainActivity.theme == MainActivity.Theme.DARK) {
            v.findViewById(R.id.top_shadow).setBackgroundResource(R.drawable.top_shadow_dark);
        }
        if (MainActivity.theme == MainActivity.Theme.AMOLED) {
            receiveButton.setBackgroundColor(android.R.color.transparent);
            sendButton.setBackgroundColor(android.R.color.transparent);
        }

        handler = new Handler();
        refreshTask = new Runnable() {
            public void run() {
                refreshSyncProgress();
            }
        };

        balanceHastings = new BigDecimal("0");
        balanceUsd = new BigDecimal("0");
        balanceText = (TextView) v.findViewById(R.id.balanceText);
        balanceUsdText = (TextView) v.findViewById(R.id.balanceUsdText);
        balanceUnconfirmedText = (TextView) v.findViewById(R.id.balanceUnconfirmed);
        transactions = new ArrayList<>();

        syncBar = (NumberProgressBar) v.findViewById(R.id.syncBar);
        syncText = (TextView) v.findViewById(R.id.syncText);
        syncBar.setProgressTextColor(MainActivity.defaultTextColor);
        walletStatusText = (TextView) v.findViewById(R.id.walletStatusText);

        transactionList = (RecyclerView) v.findViewById(R.id.transactionList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        transactionList.setLayoutManager(layoutManager);
        transactionList.addItemDecoration(new DividerItemDecoration(transactionList.getContext(), layoutManager.getOrientation()));

        sendFrame = (FrameLayout) v.findViewById(R.id.sendFrame);
        receiveFrame = (FrameLayout) v.findViewById(R.id.receiveFrame);
        unlockFrame = (FrameLayout) v.findViewById(R.id.unlockFrame);

        final WalletSendFragment sendFrag = new WalletSendFragment();
        getFragmentManager().beginTransaction().add(R.id.sendFrame, sendFrag).commit();
        final WalletReceiveFragment recvFrag = new WalletReceiveFragment();
        getFragmentManager().beginTransaction().add(R.id.receiveFrame, recvFrag).commit();
        unlockFrag = new WalletUnlockFragment();
        getFragmentManager().beginTransaction().add(R.id.unlockFrame, unlockFrag).commit();

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
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Exact Balance");
                builder.setMessage(Wallet.hastingsToSC(balanceHastings).toPlainString() + " Siacoins");
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        refresh();

        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null)
            return;
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null)
            return;
        actionBar.setTitle("Wallet");
    }

    public void refresh() {
        refreshBalanceAndStatus();
        refreshSyncProgress();
        refreshTransactions();
        //TODO: figure out a GOOD way to Toast "Refreshed" if all requests complete successfully
        //TODO: auto refresh every x seconds. Eventually add option to refresh in background, with notifications?
    }

    public void refreshBalanceAndStatus() {
        Wallet.wallet(new SiaRequest.VolleyCallback(getActivity()) {
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getString("encrypted").equals("false"))
                        walletStatusText.setText("No Wallet");
                    else if (response.getString("unlocked").equals("false"))
                        walletStatusText.setText("Locked");
                    else
                        walletStatusText.setText("Unlocked");
                    balanceHastings = new BigDecimal(response.getString("confirmedsiacoinbalance"));
                    balanceText.setText(Wallet.round(Wallet.hastingsToSC(balanceHastings)));
                    balanceHastingsUnconfirmed = new BigDecimal(response.getString("unconfirmedincomingsiacoins"))
                            .subtract(new BigDecimal(response.getString("unconfirmedoutgoingsiacoins")));
                    balanceUnconfirmedText.setText(balanceHastingsUnconfirmed.compareTo(BigDecimal.ZERO) > 0 ? "+" : "" +
                            Wallet.round(Wallet.hastingsToSC(balanceHastingsUnconfirmed)) + " unconfirmed");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (isAdded())
                    Toast.makeText(getActivity(), "Refreshed", Toast.LENGTH_SHORT).show();
            }

            public void onError(SiaRequest.Error error) {
                super.onError(error);
                balanceHastings = new BigDecimal("0");
                balanceHastingsUnconfirmed = new BigDecimal("0");
                balanceUsd = new BigDecimal("0");
                balanceText.setText(Wallet.round(balanceHastings));
                balanceUsdText.setText(Wallet.round(balanceUsd) + " USD");
                walletStatusText.setText("No wallet");
                balanceUnconfirmedText.setText(Wallet.round(new BigDecimal("0")) + " unconfirmed");
            }
        });

        Wallet.coincapSC(new Response.Listener() {
            public void onResponse(Object response) {
                try {
                    JSONObject json = new JSONObject((String) response);
                    double usdPrice = json.getDouble("usdPrice");
                    balanceUsd = Wallet.scToUsd(usdPrice, Wallet.hastingsToSC(balanceHastings));
                    balanceUsdText.setText(Wallet.round(balanceUsd) + " USD");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                balanceUsd = new BigDecimal("0");
                balanceUsdText.setText(Wallet.round(balanceUsd) + " USD");
                if (isAdded())
                    Toast.makeText(getActivity(), "Error retrieving USD value", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void refreshTransactions() {
        Wallet.transactions(new SiaRequest.VolleyCallback(getActivity()) {
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
        Consensus.consensus(new SiaRequest.VolleyCallback(getActivity()) {
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getBoolean("synced")) {
                        if (syncText.getText().equals("Syncing"))
                            syncNotification(R.drawable.ic_sync_white_48dp, "Syncing blockchain...", "Finished", false);
                        syncText.setText("Synced");
                        syncBar.setProgress(100);
                        handler.removeCallbacks(refreshTask);
                        refreshTransactions();
                        refreshBalanceAndStatus();
                    } else {
                        syncText.setText("Syncing");
                        double progress = ((double) response.getInt("height") / estimatedBlockHeightAt(System.currentTimeMillis() / 1000)) * 100;
                        syncBar.setProgress((int) progress);
                        syncNotification(R.drawable.ic_sync_white_48dp, "Syncing blockchain...", String.format("Progress (estimated): %.2f%%", progress), false);
                        handler.removeCallbacks(refreshTask);
                        handler.postDelayed(refreshTask, 60000);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onError(SiaRequest.Error error) {
                syncText.setText("Not Synced");
                syncBar.setProgress(0);
                syncNotification(R.drawable.ic_sync_problem_white_48dp, "Syncing blockchain...", "Could not retrieve sync progress", false);
                handler.removeCallbacks(refreshTask);
            }
        });
    }

    public void syncNotification(int icon, String title, String text, boolean ongoing) {
        if (!isAdded())
            return;
        Notification.Builder builder = new Notification.Builder(getActivity());
        builder.setSmallIcon(icon);
        Bitmap largeIcon = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.sia_logo_transparent);
        builder.setLargeIcon(largeIcon);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setOngoing(ongoing);
        Intent intent = new Intent(getActivity(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(SYNC_NOTIFICATION, builder.build());
    }

    // note time should be in seconds
    public int estimatedBlockHeightAt(long time) {
        long block100kTimestamp = 1492126789; // Unix timestamp; seconds
        int blockTime = 9; // overestimate
        long diff = time - block100kTimestamp;
        return (int) (100000 + (diff / 60 / blockTime));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu);
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
                Wallet.lock(new SiaRequest.VolleyCallback(getActivity()) {
                    public void onSuccess(JSONObject response) {
                        super.onSuccess(response);
                        walletStatusText.setText("Locked");
                    }
                });
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
            case R.id.actionAddSeed:
                WalletAddSeedDialog.createAndShow(getFragmentManager());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void refreshWallet(FragmentManager fragmentManager) {
        WalletFragment fragment = (WalletFragment) fragmentManager.findFragmentByTag("WalletFragment");
        if (fragment != null)
            fragment.refresh();
    }

    private TransactionExpandableGroup transactionToGroupWithChild(Transaction tx) {
        ArrayList<Transaction> child = new ArrayList<>();
        child.add(tx);
        return new TransactionExpandableGroup(tx.getNetValueStringRounded(), tx.getConfirmationDate(), child);
    }
}
