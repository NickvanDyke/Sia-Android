package vandyke.sia.transaction;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import vandyke.sia.R;
import vandyke.sia.SiaRequest;

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
        } else {
            timeString = df.format(transaction.getConfirmationDate());
        }
        holder.transactionStatus.setText(timeString);
        String valueText = SiaRequest.hastingsToSC(transaction.getNetValue()).setScale(2, BigDecimal.ROUND_FLOOR).toPlainString();
        if (!valueText.contains("-"))
            valueText = "+" + valueText;
        holder.transactionValue.setText(valueText);

        return row;
    }

    static class TransactionHolder {
        TextView transactionStatus;
        TextView transactionValue;
    }
}
