package vandyke.sia.Drawer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import vandyke.sia.R;

import java.util.ArrayList;

public class DrawerAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<DrawerItem> data;

    public DrawerAdapter(Context context, int layoutResourceId, ArrayList<DrawerItem> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        DrawerItemHolder holder;
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DrawerItemHolder();
            holder.image = (ImageView)row.findViewById(R.id.listview_item_image);
            holder.text = (TextView)row.findViewById(R.id.listview_item_text);

            row.setTag(holder);
        } else {
            holder = (DrawerItemHolder)row.getTag();
        }

        DrawerItem drawerItem = data.get(position);
        holder.image.setImageDrawable(drawerItem.image);
        holder.text.setText(drawerItem.text);

        return row;
    }

    static class DrawerItemHolder {
        ImageView image;
        TextView text;
    }
}
