package vandyke.siamobile.transactionslist;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.transaction.Transaction;
import vandyke.siamobile.transaction.TransactionInput;
import vandyke.siamobile.transaction.TransactionOutput;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionListAdapter extends ExpandableRecyclerViewAdapter<TransactionHeaderHolder, TransactionDetailsHolder> {

    private ArrayList<Transaction> data;

    private DateFormat df;

    private int red;
    private int green;

    public TransactionListAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
        df = new SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault());
        red = Color.rgb(186, 63, 63); // TODO: choose better colors maybe
        green = Color.rgb(0, 114, 11);
    }

    @Override
    public TransactionHeaderHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list_item_header, parent, false);
        return new TransactionHeaderHolder(view);
    }

    @Override
    public TransactionDetailsHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list_item_details, parent, false);
        view.findViewById(R.id.transactionInputsList).setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        view.findViewById(R.id.transactionOutputsList).setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        return new TransactionDetailsHolder(view);
    }

    @Override
    public void onBindChildViewHolder(TransactionDetailsHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        Transaction transaction = (Transaction)group.getItems().get(childIndex);

        holder.id.setText(transaction.getTransactionId());

        if (transaction.isConfirmed()) {
            holder.confirmationHeight.setText("Confirmed in block " + transaction.getConfirmationHeight());
            holder.confirmationHeight.setVisibility(View.VISIBLE);
        } else
            holder.confirmationHeight.setVisibility(View.GONE);

        ArrayList<String> inputs = new ArrayList<>();
        for (TransactionInput transactionInput : transaction.getInputs())
            inputs.add(transactionInput.toString());
        holder.inputs.setAdapter(new ArrayAdapter<>(MainActivity.instance, android.R.layout.simple_list_item_1, inputs));

        ArrayList<String> outputs = new ArrayList<>();
        for (TransactionOutput transactionOutput : transaction.getOutputs())
            outputs.add(transactionOutput.toString());
        holder.outputs.setAdapter(new ArrayAdapter<>(MainActivity.instance, android.R.layout.simple_list_item_1, outputs));
    }

    @Override
    public void onBindGroupViewHolder(TransactionHeaderHolder holder, int flatPosition, ExpandableGroup group) {
        Transaction transaction = (Transaction)group.getItems().get(0);
        String timeString;
        if (!transaction.isConfirmed()) {
            timeString = "Unconfirmed";
            holder.transactionStatus.setTextColor(Color.RED);
        } else {
            timeString = df.format(((TransactionExpandableGroup)group).getConfirmationDate());
        }
        holder.transactionStatus.setText(timeString);

        String valueText = transaction.getNetValueStringRounded();
        if (transaction.isNetZero()) {
            holder.transactionValue.setTextColor(Color.GRAY);
        } else if (valueText.contains("-")) {
            holder.transactionValue.setTextColor(red);
        } else {
            valueText = "+" + valueText;
            holder.transactionValue.setTextColor(green);
        }
        holder.transactionValue.setText(valueText);
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
            if (data.get(i).getNetValueStringRounded().equals("0.00")) {
                data.remove(i);
                changed = true;
                i--;
            }
        if (changed)
            notifyDataSetChanged();
        return changed;
    }
}
