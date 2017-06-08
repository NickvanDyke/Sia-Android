package vandyke.siamobile.transaction;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.Wallet;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TransactionListAdapter extends ArrayAdapter {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Transaction> data;

    private DateFormat df;

    private int red;
    private int green;

    private boolean hideZero = true;

    public TransactionListAdapter(Context context, int layoutResourceId, ArrayList<Transaction> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
        df = new SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault());
        red = Color.rgb(186, 63, 63); // TODO: choose better colors maybe
        green = Color.rgb(0, 114, 11);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TransactionHolder holder;
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new TransactionHolder();
            holder.transactionStatus = (TextView) row.findViewById(R.id.transactionStatus);
            holder.transactionValue = (TextView) row.findViewById(R.id.transactionValue);

            row.setTag(holder);
        } else {
            holder = (TransactionHolder) row.getTag();
        }

        Transaction transaction = data.get(position);

        String timeString;
        if (transaction.getConfirmationDate() == null) {
            timeString = "Unconfirmed";
            holder.transactionStatus.setTextColor(Color.RED);
        } else {
            timeString = df.format(transaction.getConfirmationDate());
        }
        holder.transactionStatus.setText(timeString);

        String valueText = Wallet.hastingsToSC(transaction.getNetValue()).setScale(2, BigDecimal.ROUND_FLOOR).toPlainString();
        if (valueText.equals("0.00")) {
            holder.transactionValue.setTextColor(Color.GRAY);
        } else if (valueText.contains("-")) {
            holder.transactionValue.setTextColor(red);
        } else {
            valueText = "+" + valueText;
            holder.transactionValue.setTextColor(green);
        }
        holder.transactionValue.setText(valueText);

        return row;
    }

    public void setData(ArrayList<Transaction> data) {
        this.data = new ArrayList<>(data);
        if (MainActivity.prefs.getBoolean("hideZero", false)) {
            if (!removeZeroTransactions())
                notifyDataSetChanged();
        } else
            notifyDataSetChanged();
    }

    public boolean removeZeroTransactions() {
        boolean changed = false;
        for (int i = 0; i < data.size(); i++)
            if (data.get(i).getNetValueString().equals("0.00")) {
                data.remove(i);
                changed = true;
                i--;
            }
        if (changed)
            notifyDataSetChanged();
        return changed;
    }

    public int getCount() {
        return data.size();
    }

    static class TransactionHolder {
        TextView transactionStatus;
        TextView transactionValue;
    }
}
