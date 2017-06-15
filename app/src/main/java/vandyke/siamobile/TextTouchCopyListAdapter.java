package vandyke.siamobile;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import vandyke.siamobile.R;

import java.util.ArrayList;

// TODO: this class might not be needed. remove if that's the case
public class TextTouchCopyListAdapter extends ArrayAdapter {

    private final int layoutResourceId;
    private final Context context;
    private ArrayList<String> data;

    public TextTouchCopyListAdapter(Context context, int layoutResourceId, ArrayList<String> data) {
        super(context, layoutResourceId);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.textView = (TextView)row.findViewById(R.id.listTextView);

            row.setTag(holder);
        } else {
            holder = (Holder)row.getTag();
        }

        String text = data.get(position);
        holder.textView.setText(text);

        return row;
    }

    public int getCount() {
        return data.size();
    }

    public ArrayList<String> getData() {
        return data;
    }

    static class Holder {
        TextView textView;
    }
}
