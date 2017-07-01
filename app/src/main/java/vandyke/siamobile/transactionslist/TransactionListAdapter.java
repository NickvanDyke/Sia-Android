package vandyke.siamobile.transactionslist;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.transaction.EmptyTransaction;
import vandyke.siamobile.transaction.Transaction;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TransactionListAdapter extends RecyclerView.Adapter {

    private ArrayList<Transaction> data;
    private DateFormat df;
    private boolean ads;

    private int red;
    private int green;

    private int TYPE_TX = 0;
    private int TYPE_AD = 1;

    private int TX_PER_AD = 5;

    public TransactionListAdapter(ArrayList<Transaction> data) {
        super();
        ads = MainActivity.prefs.getBoolean("adsEnabled", true);
        this.data = data;
        df = new SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault());
        red = Color.rgb(186, 63, 63); // TODO: choose better colors maybe
        green = Color.rgb(0, 114, 11);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        if (viewType == TYPE_TX) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tx_header, parent, false);
            return new TransactionHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_layout, parent, false);
            return new NativeAdHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TransactionHolder) {
            TransactionHolder txHolder = (TransactionHolder)holder;
            Transaction transaction = data.get(position);
            String timeString;
            if (!transaction.isConfirmed()) {
                timeString = "Unconfirmed";
                txHolder.transactionStatus.setTextColor(Color.RED);
            } else {
                timeString = df.format(transaction.getConfirmationDate());
                txHolder.transactionStatus.setTextColor(MainActivity.defaultTextColor);
            }
            txHolder.transactionStatus.setText(timeString);

            String id = transaction.getTransactionId();
            txHolder.transactionId.setText(id.substring(0, id.length() / 2) + "\n" + id.substring(id.length() / 2));

            String valueText = transaction.getNetValueStringRounded();
            if (transaction.isNetZero() || transaction.getNetValueStringExact().equals("")) {
                txHolder.transactionValue.setTextColor(MainActivity.defaultTextColor);
            } else if (valueText.contains("-")) {
                txHolder.transactionValue.setTextColor(red);
            } else {
                valueText = "+" + valueText;
                txHolder.transactionValue.setTextColor(green);
            }
            txHolder.transactionValue.setText(valueText);
        } else {

        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position) == null ? TYPE_AD : TYPE_TX;
    }

    public void setData(ArrayList<Transaction> data) {
        ads = MainActivity.prefs.getBoolean("adsEnabled", true);
        if (data.size() == 0 && ads) {
            this.data = new ArrayList<>();
            this.data.add(new EmptyTransaction());
            for (int i = 0; i < 6; i ++)
                this.data.add(null);
            return;
        }
        this.data = new ArrayList<>(data);
        if (MainActivity.prefs.getBoolean("hideZero", false))
            removeZeroTransactions();
        if (ads)
            insertNullsForAds();
        notifyDataSetChanged();
    }

    public void removeZeroTransactions() {
        BigDecimal zero = new BigDecimal("0");
        for (int i = 0; i < data.size(); i++)
            if (data.get(i).getNetValue().compareTo(zero) == 0) {
                data.remove(i);
                i--;
            }
    }

    public void insertNullsForAds() {
        for (int i = 0; i < data.size(); i++) {
            if (i % TX_PER_AD == 0)
                data.add(i, null);
        }
    }
}
