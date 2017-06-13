package vandyke.siamobile.transaction;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.Wallet;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TransactionListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private int groupLayoutResourceId;
    private int childLayoutResourceId;
    private ArrayList<Transaction> data;

    private DateFormat df;

    private int red;
    private int green;

    public TransactionListAdapter(Context context, int groupLayoutResourceId, int childLayoutResourceId, ArrayList<Transaction> data) {
        this.context = context;
        this.groupLayoutResourceId = groupLayoutResourceId;
        this.childLayoutResourceId = childLayoutResourceId;
        this.data = data;
        df = new SimpleDateFormat("MMM dd\nh:mm a", Locale.getDefault());
        red = Color.rgb(186, 63, 63); // TODO: choose better colors maybe
        green = Color.rgb(0, 114, 11);
    }

    @Override
    public View getGroupView(int position, boolean b, View view, ViewGroup viewGroup) {
        TransactionHeaderHolder holder;

        if (view == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(groupLayoutResourceId, viewGroup, false);

            holder = new TransactionHeaderHolder();
            holder.transactionStatus = (TextView)view.findViewById(R.id.transactionStatus);
            holder.transactionValue = (TextView)view.findViewById(R.id.transactionValue);

            view.setTag(holder);
        } else {
            holder = (TransactionHeaderHolder)view.getTag();
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

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View view, ViewGroup viewGroup) {
        TransactionDetailsHolder holder;
        if (view == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(childLayoutResourceId, viewGroup, false);

            holder = new TransactionDetailsHolder();
            holder.id = (TextView)view.findViewById(R.id.transactionDetailsId);
            holder.confirmationHeight = (TextView)view.findViewById(R.id.transactionDetailsConfirmHeight);
            holder.inputs = (ListView)view.findViewById(R.id.transactionInputsList);
            holder.inputs.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    // disallow the onTouch for your scrollable parent view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            holder.outputs = (ListView)view.findViewById(R.id.transactionInputsList);
            holder.outputs.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            view.setTag(holder);
        } else {
            holder = (TransactionDetailsHolder)view.getTag();
        }

        Transaction transaction = data.get(groupPosition);

        holder.id.setText("ID: " + transaction.getTransactionId());

        if (transaction.isConfirmed()) {
            holder.confirmationHeight.setText("Confirmed in block " + transaction.getConfirmationHeight());
            holder.confirmationHeight.setVisibility(View.VISIBLE);
        } else
            holder.confirmationHeight.setVisibility(View.GONE);

        ArrayList<String> inputs = new ArrayList<>();
        for (TransactionInput transactionInput : transaction.getInputs())
            inputs.add(transactionInput.toString());
        holder.inputs.setAdapter(new ArrayAdapter<>(viewGroup.getContext(), R.layout.text_touch_copy_list_item, inputs));

        ArrayList<String> outputs = new ArrayList<>();
        for (TransactionOutput transactionOutput : transaction.getOutputs())
            outputs.add(transactionOutput.toString());
        holder.outputs.setAdapter(new ArrayAdapter<>(viewGroup.getContext(), R.layout.text_touch_copy_list_item, outputs));

        return view;
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

    @Override
    public int getGroupCount() {
        return data.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int position) {
        return data.get(position);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return data.get(groupPosition);
    }

    @Override
    public long getGroupId(int position) {
        return position;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    static class TransactionHeaderHolder {
        TextView transactionStatus;
        TextView transactionValue;
    }

    static class TransactionDetailsHolder {
        TextView id;
        TextView confirmationHeight;
        ListView inputs;
        ListView outputs;
    }
}
