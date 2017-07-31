/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.transactionslist;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.models.TransactionInputModel;
import vandyke.siamobile.api.models.TransactionModel;
import vandyke.siamobile.api.models.TransactionOutputModel;
import vandyke.siamobile.misc.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionListAdapter extends ExpandableRecyclerViewAdapter<TransactionHeaderHolder, TransactionDetailsHolder> {

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
    public TransactionHeaderHolder onCreateGroupViewHolder(final ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tx_header, parent, false);
        // TODO: find how to make the view not expand if it's long pressed... spent a long time and still couldn't get it, idk if possible in this situation
        final TextView idText = (TextView) view.findViewById(R.id.transactionHeaderId);
        idText.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                ClipData clip = ClipData.newPlainText("Sia transaction id", ((TextView)v).getText());
                ((ClipboardManager) idText.getContext().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(clip);
                Utils.INSTANCE.snackbar(view, "Copied transaction ID", Snackbar.LENGTH_SHORT);
                return true;
            }
        });
        idText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                event.addBatch(System.nanoTime(), event.getX() + v.getLeft(), event.getY() + v.getTop(), 1, 1, MotionEvent.ACTION_DOWN);
                ((View) v.getParent()).onTouchEvent(event);
                return false;
            }
        });
        return new TransactionHeaderHolder(view);
    }

    @Override
    public TransactionDetailsHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tx_details, parent, false);
        if (MainActivity.Companion.getAppTheme() == MainActivity.Theme.AMOLED || MainActivity.Companion.getAppTheme() == MainActivity.Theme.CUSTOM) {
            view.findViewById(R.id.top_shadow).setVisibility(View.GONE);
            view.findViewById(R.id.bot_shadow).setVisibility(View.GONE);
        } else if (MainActivity.Companion.getAppTheme() == MainActivity.Theme.DARK) {
            view.findViewById(R.id.top_shadow).setBackgroundResource(R.drawable.top_shadow_dark);
            view.findViewById(R.id.bot_shadow).setBackgroundResource(R.drawable.bot_shadow_dark);
        }

        ListView inputsList = (ListView) view.findViewById(R.id.transactionInputsList);
        ListView outputsList = (ListView) view.findViewById(R.id.transactionOutputsList);

        if (MainActivity.Companion.getAppTheme() == MainActivity.Theme.CUSTOM) {
            inputsList.setBackgroundColor(android.R.color.transparent);
            outputsList.setBackgroundColor(android.R.color.transparent);
        } else {
            inputsList.setBackgroundColor(MainActivity.Companion.getBackgroundColor());
            outputsList.setBackgroundColor(MainActivity.Companion.getBackgroundColor());
        }
        return new TransactionDetailsHolder(view);
    }

    @Override
    public void onBindChildViewHolder(TransactionDetailsHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        TransactionModel transaction = (TransactionModel) group.getItems().get(childIndex);

        if (transaction.getConfirmed()) {
            holder.confirmationHeight.setText("Confirmation block: " + transaction.getConfirmationheight());
        } else
            holder.confirmationHeight.setText("Unconfirmed");

        ArrayList<TransactionInputModel> inputs = new ArrayList<>();
        inputs.addAll(transaction.getInputs());
//        holder.inputs.setAdapter(new TransactionIOAdapter(holder.inputs.getContext(), R.layout.list_item_tx_io, inputs));

        ArrayList<TransactionOutputModel> outputs = new ArrayList<>();
        outputs.addAll(transaction.getOutputs());
//        holder.outputs.setAdapter(new TransactionIOAdapter(holder.outputs.getContext(), R.layout.list_item_tx_io, outputs));

        if (inputs.size() > 1)
            holder.inputs.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
        else
            holder.inputs.setOnTouchListener(null);

        if (outputs.size() > 1)
            holder.outputs.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
        else
            holder.outputs.setOnTouchListener(null);
    }

    @Override
    public void onBindGroupViewHolder(TransactionHeaderHolder holder, int flatPosition, ExpandableGroup group) {
        TransactionModel transaction = (TransactionModel) group.getItems().get(0);
        String timeString;
        if (!transaction.getConfirmed()) {
            timeString = "Unconfirmed";
            holder.transactionStatus.setTextColor(Color.RED);
        } else {
            timeString = df.format(((TransactionExpandableGroup) group).getConfirmationDate());
            holder.transactionStatus.setTextColor(MainActivity.Companion.getDefaultTextColor());
        }
        holder.transactionStatus.setText(timeString);

        String id = transaction.getTransactionid();
        holder.transactionId.setText(id.substring(0, id.length() / 2) + "\n" + id.substring(id.length() / 2));

        String valueText = transaction.getNetValueStringRounded();
        if (transaction.isNetZero()) {
            holder.transactionValue.setTextColor(MainActivity.Companion.getDefaultTextColor());
        } else if (valueText.contains("-")) {
            holder.transactionValue.setTextColor(red);
        } else {
            valueText = "+" + valueText;
            holder.transactionValue.setTextColor(green);
        }
        holder.transactionValue.setText(valueText);
    }

//    public void setData(ArrayList<Transaction> data) {
//        this.data = new ArrayList<>(data);
//        if (MainActivity.prefs.getBoolean("hideZero", false)) {
//            if (!removeZeroTransactions())
//                notifyDataSetChanged();
//        } else
//            notifyDataSetChanged();
//    }
//
//    public boolean removeZeroTransactions() {
//        boolean changed = false;
//        for (int i = 0; i < data.size(); i++)
//            if (data.get(i).getNetValueStringRounded().equals("0.00")) {
//                data.remove(i);
//                changed = true;
//                i--;
//            }
//        if (changed)
//            notifyDataSetChanged();
//        return changed;
//    }
}
