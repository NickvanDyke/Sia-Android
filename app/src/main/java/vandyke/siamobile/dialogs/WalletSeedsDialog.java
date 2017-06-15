package vandyke.siamobile.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.TextTouchCopyListAdapter;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

import java.util.ArrayList;

public class WalletSeedsDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_seeds, null);
        ListView seedsList = (ListView) view.findViewById(R.id.seedsList);
        final ArrayList<String> seeds = new ArrayList<>();
        final TextTouchCopyListAdapter adapter = new TextTouchCopyListAdapter(MainActivity.instance, R.layout.text_touch_copy_list_item, seeds);
        seedsList.setAdapter(adapter);
        Wallet.seeds("english", new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray seedsJson = response.getJSONArray("allseeds");
                    for (int i = 0;i < seedsJson.length(); i++)
                        seeds.add(seedsJson.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
            }
        });

        builder.setTitle("Wallet Seeds")
                .setView(view)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_wallet_seeds, null);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new WalletSeedsDialog().show(fragmentManager, "view seeds dialog");
    }
}
