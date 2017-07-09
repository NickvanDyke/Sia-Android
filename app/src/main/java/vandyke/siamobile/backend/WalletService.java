/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.R;
import vandyke.siamobile.api.Consensus;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.misc.SiaMobileApplication;
import vandyke.siamobile.misc.Utils;
import vandyke.siamobile.wallet.transaction.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;

public class WalletService extends Service {

    private final IBinder binder = new LocalBinder();

    public enum WalletStatus {
        NONE, LOCKED, UNLOCKED
    }

    private WalletStatus walletStatus;
    private BigDecimal balanceHastings;
    private BigDecimal balanceHastingsUnconfirmed;
    private BigDecimal balanceUsd;
    private ArrayList<Transaction> transactions;
    private double syncProgress;

    private ArrayList<WalletUpdateListener> listeners;

    private Handler handler;
    private Runnable refreshRunnable;

    private int SYNC_NOTIFICATION = 0;
    private int TRANSACTION_NOTIFICATION = 1;

    public void refreshAll() {
        refreshBalanceAndStatus();
        refreshTransactions();
        refreshSyncProgress();
    }

    public void refreshBalanceAndStatus() {
        Wallet.wallet(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getString("encrypted").equals("false"))
                        walletStatus = WalletStatus.NONE;
                    else if (response.getString("unlocked").equals("false"))
                        walletStatus = WalletStatus.LOCKED;
                    else
                        walletStatus = WalletStatus.UNLOCKED;
                    balanceHastings = new BigDecimal(response.getString("confirmedsiacoinbalance"));
                    balanceHastingsUnconfirmed = new BigDecimal(response.getString("unconfirmedincomingsiacoins"))
                            .subtract(new BigDecimal(response.getString("unconfirmedoutgoingsiacoins")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendBalanceUpdate();
            }

            public void onError(SiaRequest.Error error) {
                sendBalanceError(error);
            }
        });

        Wallet.coincapSC(new Response.Listener() {
            public void onResponse(Object response) {
                try {
                    JSONObject json = new JSONObject((String) response);
                    double usdPrice = json.getDouble("usdPrice");
                    balanceUsd = Wallet.scToUsd(usdPrice, Wallet.hastingsToSC(balanceHastings));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendUsdUpdate();
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                sendUsdError(error);
            }
        });
    }

    public void refreshTransactions() {
        Wallet.transactions(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                String mostRecentTxId = SiaMobileApplication.prefs.getString("mostRecentTxId", "0"); // TODO: can give false positives when switching between wallets
                int newTxs = 0;
                boolean foundMostRecent = false;
                BigDecimal netOfNewTxs = new BigDecimal("0");
                for (Transaction tx : Transaction.populateTransactions(response)) {
                    if (tx.getTransactionId().equals(mostRecentTxId))
                        foundMostRecent = true;
                    if (!transactions.contains(tx)) {
                        transactions.add(tx);
                        if (!foundMostRecent) {
                            newTxs++;
                            netOfNewTxs = netOfNewTxs.add(tx.getNetValue());
                        }
                    }
                }
                if (newTxs > 0) {
                    SiaMobileApplication.prefs.edit().putString("mostRecentTxId", transactions.get(0).getTransactionId()).apply();
                    Utils.notification(WalletService.this, TRANSACTION_NOTIFICATION,
                            R.drawable.ic_account_balance_white_48dp, newTxs + " new transaction" + (newTxs > 1 ? "s" : ""),
                            "Net value: " + (netOfNewTxs.compareTo(BigDecimal.ZERO) > 0 ? "+" : "") + Wallet.round(Wallet.hastingsToSC(netOfNewTxs)) + " SC",
                            false);
                }
                sendTransactionsUpdate();
            }

            public void onError(SiaRequest.Error error) {
                sendTransactionsError(error);
            }
        });
    }

    public void refreshSyncProgress() {
        Consensus.consensus(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getBoolean("synced")) {
                        syncProgress = 100;
                        Utils.cancelNotification(WalletService.this, SYNC_NOTIFICATION); // TODO: maybe have separate service for notifications that registers a listener... not sure if worth it
                    } else {
                        syncProgress = ((double) response.getInt("height") / estimatedBlockHeightAt(System.currentTimeMillis() / 1000)) * 100;
                        Utils.notification(WalletService.this, SYNC_NOTIFICATION, R.drawable.ic_sync_white_48dp,
                                "Syncing blockchain...", String.format("Progress (estimated): %.2f%", syncProgress), false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendSyncUpdate();
            }

            public void onError(SiaRequest.Error error) {
                sendSyncError(error);
                Utils.notification(WalletService.this, SYNC_NOTIFICATION, R.drawable.ic_sync_problem_white_48dp,
                        "Syncing blockchain...", "Error retrieving sync progress", false);
            }
        });
    }

    @Override
    public void onCreate() {
        listeners = new ArrayList<>();
        walletStatus = WalletStatus.NONE;
        balanceHastings = new BigDecimal("0");
        balanceHastingsUnconfirmed = new BigDecimal("0");
        balanceUsd = new BigDecimal("0");
        transactions = new ArrayList<>();
        syncProgress = 0;
        handler = new Handler();
        postRefreshRunnable();
    }

    public void postRefreshRunnable() {
        if (refreshRunnable != null)
            handler.removeCallbacks(refreshRunnable);
        final long refreshInterval = 60000 * Long.parseLong(SiaMobileApplication.prefs.getString("monitorRefreshInterval", "1"));
        refreshRunnable = new Runnable() {
            public void run() {
                refreshAll();
                handler.postDelayed(refreshRunnable, refreshInterval);
            }
        };
        if (refreshInterval != 0)
            handler.post(refreshRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (!SiaMobileApplication.prefs.getBoolean("monitorInBackground", true)) {
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public WalletService getService() {
            return WalletService.this;
        }
    }

    public interface WalletUpdateListener {
        void onBalanceUpdate(WalletService service);

        void onUsdUpdate(WalletService service);

        void onTransactionsUpdate(WalletService service);

        void onSyncUpdate(WalletService service);

        void onBalanceError(SiaRequest.Error error);

        void onUsdError(VolleyError error);

        void onTransactionsError(SiaRequest.Error error);

        void onSyncError(SiaRequest.Error error);
    }

    public void registerListener(WalletUpdateListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(WalletUpdateListener listener) {
        listeners.remove(listener);
    }

    public void sendBalanceUpdate() {
        for (WalletUpdateListener listener : listeners)
            listener.onBalanceUpdate(this);
    }

    public void sendUsdUpdate() {
        for (WalletUpdateListener listener : listeners)
            listener.onUsdUpdate(this);
    }

    public void sendTransactionsUpdate() {
        for (WalletUpdateListener listener : listeners)
            listener.onTransactionsUpdate(this);
    }

    public void sendSyncUpdate() {
        for (WalletUpdateListener listener : listeners)
            listener.onSyncUpdate(this);
    }

    public void sendBalanceError(SiaRequest.Error error) {
        for (WalletUpdateListener listener : listeners)
            listener.onBalanceError(error);
    }

    public void sendUsdError(VolleyError error) {
        for (WalletUpdateListener listener : listeners)
            listener.onUsdError(error);
    }

    public void sendTransactionsError(SiaRequest.Error error) {
        for (WalletUpdateListener listener : listeners)
            listener.onTransactionsError(error);
    }

    public void sendSyncError(SiaRequest.Error error) {
        for (WalletUpdateListener listener : listeners)
            listener.onSyncError(error);
    }

    public WalletStatus getWalletStatus() {
        return walletStatus;
    }

    public BigDecimal getBalanceHastings() {
        return balanceHastings;
    }

    public BigDecimal getBalanceHastingsUnconfirmed() {
        return balanceHastingsUnconfirmed;
    }

    public BigDecimal getBalanceUsd() {
        return balanceUsd;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public double getSyncProgress() {
        return syncProgress;
    }

    public int estimatedBlockHeightAt(long time) {
        long block100kTimestamp = 1492126789; // Unix timestamp; seconds
        int blockTime = 9; // overestimate
        long diff = time - block100kTimestamp;
        return (int) (100000 + (diff / 60 / blockTime));
    }
}
