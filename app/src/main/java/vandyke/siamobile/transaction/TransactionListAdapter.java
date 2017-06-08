package vandyke.siamobile.transaction;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
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

    public TransactionListAdapter(Context context, int layoutResourceId, ArrayList<Transaction> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
        df = new SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault());
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TransactionHolder holder;
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new TransactionHolder();
            holder.transactionStatus = (TextView)row.findViewById(R.id.transactionStatus);
            holder.transactionValue = (TextView)row.findViewById(R.id.transactionValue);

            row.setTag(holder);
        } else {
            holder = (TransactionHolder)row.getTag();
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
        if (valueText.contains("-")) {
            holder.transactionValue.setTextColor(Color.RED);
        } else {
            valueText = "+" + valueText;
            holder.transactionValue.setTextColor(Color.GREEN);
        }
        holder.transactionValue.setText(valueText);

        return row;
    }

    static class TransactionHolder {
        TextView transactionStatus;
        TextView transactionValue;
    }
}
