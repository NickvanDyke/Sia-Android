/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.fragments;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.daimajia.numberprogressbar.NumberProgressBar;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.backend.WalletService;
import vandyke.siamobile.wallet.transaction.Transaction;
import vandyke.siamobile.wallet.transactionslist.TransactionExpandableGroup;
import vandyke.siamobile.wallet.transactionslist.TransactionListAdapter;

import java.math.BigDecimal;
import java.util.ArrayList;

public class WalletFragment extends Fragment {

    public static int SYNC_NOTIFICATION = 0;
    private Handler handler;
    private Runnable refreshTask;

    private TextView balanceText;
    private TextView balanceUsdText;
    private TextView balanceUnconfirmedText;
    private NumberProgressBar syncBar;
    private TextView syncText;
    private TextView walletStatusText;
    private RecyclerView transactionList;
    private final ArrayList<TransactionExpandableGroup> transactionExpandableGroups = new ArrayList<>();
    private FrameLayout expandFrame;

    private View view;

    private ServiceConnection connection;
    private WalletService walletService;
    private boolean bound = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_wallet, container, false);
        setHasOptionsMenu(true);

        final Button receiveButton = (Button) view.findViewById(R.id.receiveButton);
        final Button sendButton = (Button) view.findViewById(R.id.sendButton);

        if (MainActivity.theme == MainActivity.Theme.AMOLED || MainActivity.theme == MainActivity.Theme.CUSTOM) {
            view.findViewById(R.id.top_shadow).setVisibility(View.GONE);
        } else if (MainActivity.theme == MainActivity.Theme.DARK) {
            view.findViewById(R.id.top_shadow).setBackgroundResource(R.drawable.top_shadow_dark);
        }
        if (MainActivity.theme == MainActivity.Theme.AMOLED) {
            receiveButton.setBackgroundColor(android.R.color.transparent);
            sendButton.setBackgroundColor(android.R.color.transparent);
        }

        handler = new Handler();
        refreshTask = new Runnable() {
            public void run() {
                refreshSyncProgress();
                handler.postDelayed(refreshTask, 60000);
            }
        };

        balanceText = (TextView) view.findViewById(R.id.balanceText);
        balanceUsdText = (TextView) view.findViewById(R.id.balanceUsdText);
        balanceUnconfirmedText = (TextView) view.findViewById(R.id.balanceUnconfirmed);

        syncBar = (NumberProgressBar) view.findViewById(R.id.syncBar);
        syncText = (TextView) view.findViewById(R.id.syncText);
        syncBar.setProgressTextColor(MainActivity.defaultTextColor);
        walletStatusText = (TextView) view.findViewById(R.id.walletStatusText);

        transactionList = (RecyclerView) view.findViewById(R.id.transactionList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        transactionList.setLayoutManager(layoutManager);
        transactionList.addItemDecoration(new DividerItemDecoration(transactionList.getContext(), layoutManager.getOrientation()));

        expandFrame = (FrameLayout) view.findViewById(R.id.expandFrame);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceExpandFrame(new WalletSendFragment());
            }
        });
        receiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                replaceExpandFrame(new WalletReceiveFragment());
            }
        });

        balanceText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Exact Balance");
                builder.setMessage(Wallet.hastingsToSC(walletService.getBalanceHastings()).toPlainString() + " Siacoins");
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

//        refreshAll();
        handler.postDelayed(refreshTask, 60000);

        return view;
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

        connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                walletService = ((WalletService.LocalBinder) service).getService();
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        getActivity().bindService(new Intent(getActivity(), WalletService.class), connection, Context.BIND_AUTO_CREATE);
    }

    public void refreshAll() {
        refreshBalanceAndStatus();
        refreshSyncProgress();
        refreshTransactions();
        //TODO: figure out a GOOD way to Toast "Refreshed" if all requests complete successfully
        //TODO: auto refreshAll every x seconds. Eventually add option to refreshAll in background, with notifications?
    }

    public void refreshBalanceAndStatus() {
        if (!bound)
            return;
        switch (walletService.getWalletStatus()) {
            case NONE:
                walletStatusText.setText("No Wallet");
                break;
            case LOCKED:
                walletStatusText.setText("Locked");
                break;
            case UNLOCKED:
                walletStatusText.setText("Unlocked");
        }
        balanceText.setText(Wallet.round(Wallet.hastingsToSC(walletService.getBalanceHastings())));
        balanceUnconfirmedText.setText(walletService.getBalanceHastingsUnconfirmed().compareTo(BigDecimal.ZERO) > 0 ? "+" : "" +
                Wallet.round(Wallet.hastingsToSC(walletService.getBalanceHastingsUnconfirmed())) + " unconfirmed");
        balanceUsdText.setText(Wallet.round(walletService.getBalanceUsd()) + " USD");
    }

    public void refreshTransactions() {
        if (!bound)
            return;
        boolean hideZero = MainActivity.prefs.getBoolean("hideZero", false);
        transactionExpandableGroups.clear();
        for (Transaction tx : walletService.getTransactions()) {
            if (hideZero && tx.isNetZero())
                continue;
            transactionExpandableGroups.add(transactionToGroupWithChild(tx));
        }
        transactionList.setAdapter(new TransactionListAdapter(transactionExpandableGroups));
    }

    public void refreshSyncProgress() {
        if (!bound)
            return;
        double syncProgress = walletService.getSyncProgress();
        if (syncProgress == 100) {
            syncText.setText("Synced");
            syncBar.setProgress(100);
//            syncNotification(R.drawable.ic_sync_white_48dp, "Syncing blockchain...", "Finished", false);
        } else if (syncProgress == 0) {
            syncText.setText("Not Synced");
            syncBar.setProgress(0);
//            syncNotification(R.drawable.ic_sync_problem_white_48dp, "Syncing blockchain...", "Could not retrieve sync progress", false);
        } else {
            syncText.setText("Syncing");
            syncBar.setProgress((int)syncProgress);
//            syncNotification(R.drawable.ic_sync_white_48dp, "Syncing blockchain...", String.format("Progress (estimated): %.2f%%", syncProgress), false);
        }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionRefresh:
                if (bound)
                    walletService.refreshAll();
                refreshAll();
                break;
            case R.id.actionUnlock:
                replaceExpandFrame(new WalletUnlockFragment());
                break;
            case R.id.actionLock:
                Wallet.lock(new SiaRequest.VolleyCallback(view) {
                    public void onSuccess(JSONObject response) {
                        super.onSuccess(response);
                        walletStatusText.setText("Locked");
                    }
                });
                break;
            case R.id.actionChangePassword:
                replaceExpandFrame(new WalletChangePasswordFragment());
                break;
            case R.id.actionViewSeeds:
                replaceExpandFrame(new WalletSeedsFragment());
                break;
            case R.id.actionCreateWallet:
                replaceExpandFrame(new WalletCreateFragment());
                break;
            case R.id.actionSweepSeed:
                replaceExpandFrame(new WalletSweepSeedFragment());
                break;
            case R.id.actionViewAddresses:
                replaceExpandFrame(new WalletAddressesFragment());
                break;
            case R.id.actionAddSeed:
                replaceExpandFrame(new WalletAddSeedFragment());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void replaceExpandFrame(Fragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.expandFrame, fragment).commit();
        expandFrame.setVisibility(View.VISIBLE);
    }

    private TransactionExpandableGroup transactionToGroupWithChild(Transaction tx) {
        ArrayList<Transaction> child = new ArrayList<>();
        child.add(tx);
        return new TransactionExpandableGroup(tx.getNetValueStringRounded(), tx.getConfirmationDate(), child);
    }

    public void onStop() {
        super.onStop();
        if (bound && isAdded()) {
            getActivity().unbindService(connection);
            bound = false;
        }
    }
}
