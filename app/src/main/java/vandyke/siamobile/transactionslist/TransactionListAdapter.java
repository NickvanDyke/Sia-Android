package vandyke.siamobile.transactionslist;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.transaction.Transaction;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TransactionListAdapter extends RecyclerView.Adapter<TransactionHeaderHolder> {

    private ArrayList<Transaction> data;
    private DateFormat df;

    private int red;
    private int green;

    public TransactionListAdapter(ArrayList<Transaction> data) {
        super();
        this.data = data;
        df = new SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault());
        red = Color.rgb(186, 63, 63); // TODO: choose better colors maybe
        green = Color.rgb(0, 114, 11);
    }

    @Override
    public TransactionHeaderHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tx_header, parent, false);
        // TODO: find how to make the view not expand if it's long pressed... spent a long time and still couldn't get it, idk if possible in this situation
        final TextView idText = (TextView) view.findViewById(R.id.transactionHeaderId);
        idText.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                ClipData clip = ClipData.newPlainText("Sia transaction id", ((TextView)v).getText());
                ((ClipboardManager) idText.getContext().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(clip);
                Toast.makeText(idText.getContext(), "Copied transaction ID", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        return new TransactionHeaderHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionHeaderHolder holder, int position) {
        Transaction transaction = data.get(position);
        String timeString;
        if (!transaction.isConfirmed()) {
            timeString = "Unconfirmed";
            holder.transactionStatus.setTextColor(Color.RED);
        } else {
            timeString = df.format(transaction.getConfirmationDate());
            holder.transactionStatus.setTextColor(MainActivity.defaultTextColor);
        }
        holder.transactionStatus.setText(timeString);

        String id = transaction.getTransactionId();
        holder.transactionId.setText(id.substring(0, id.length() / 2) + "\n" + id.substring(id.length() / 2));

        String valueText = transaction.getNetValueStringRounded();
        if (transaction.isNetZero()) {
            holder.transactionValue.setTextColor(MainActivity.defaultTextColor);
        } else if (valueText.contains("-")) {
            holder.transactionValue.setTextColor(red);
        } else {
            valueText = "+" + valueText;
            holder.transactionValue.setTextColor(green);
        }
        holder.transactionValue.setText(valueText);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(ArrayList<Transaction> data) {
        this.data = new ArrayList<>(data);
        if (MainActivity.prefs.getBoolean("hideZero", false))
            removeZeroTransactions();
        notifyDataSetChanged();
    }
//
    public void removeZeroTransactions() {
        BigDecimal zero = new BigDecimal("0");
        for (int i = 0; i < data.size(); i++)
            if (data.get(i).getNetValue().compareTo(zero) == 0) {
                data.remove(i);
                i--;
            }
    }
}
