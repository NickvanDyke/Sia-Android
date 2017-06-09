package vandyke.siamobile.dialogs;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;

import java.util.ArrayList;

public class DisplaySeedsListAdapter extends ArrayAdapter {

    private final int layoutResourceId;
    private final Context context;
    private static ArrayList<String> seeds;

    public DisplaySeedsListAdapter(Context context, int layoutResourceId, ArrayList<String> data) {
        super(context, layoutResourceId);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        seeds = data;
        final DisplaySeedsListAdapter dis = this;
    }

    // TODO: this isn't being called
    public View getView(int position, View convertView, ViewGroup parent) {
        SeedHolder holder;
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new SeedHolder();
            holder.seed = (TextView)row.findViewById(R.id.seedText);
            holder.copyButton = (Button)row.findViewById(R.id.copySeed);

            row.setTag(holder);
        } else {
            holder = (SeedHolder)row.getTag();
        }

        System.out.println("hi");

        String seed = seeds.get(position);
        holder.seed.setText(seed);
        final TextView tempSeedTextView = holder.seed;
        holder.copyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) MainActivity.instance.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("wallet seed", tempSeedTextView.getText());
                clipboard.setPrimaryClip(clip);
            }
        });

        return row;
    }

    public int getCount() {
        return seeds.size();
    }

    static class SeedHolder {
        TextView seed;
        Button copyButton;
    }
}
