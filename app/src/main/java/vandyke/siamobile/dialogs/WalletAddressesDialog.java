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

public class WalletAddressesDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        if (MainActivity.darkMode)
            builder = new AlertDialog.Builder(getActivity(), R.style.DarkDialogTheme);
        else
            builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_addresses, null);
        ListView seedsList = (ListView)view.findViewById(R.id.addressesList);
        final ArrayList<String> addresses = new ArrayList<>();
        final TextTouchCopyListAdapter adapter = new TextTouchCopyListAdapter(MainActivity.instance, R.layout.text_touch_copy_list_item, addresses);
        seedsList.setAdapter(adapter);
        Wallet.addresses(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray addressesJson = response.getJSONArray("addresses");
                    for (int i = 0; i < addressesJson.length(); i++)
                        addresses.add(addressesJson.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
            }
        });

        builder.setTitle("Wallet Addresses")
                .setView(view)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_wallet_addresses, null);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new WalletAddressesDialog().show(fragmentManager, "view addresses dialog");
    }
}
