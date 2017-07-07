package vandyke.siamobile.wallet.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.misc.TextTouchCopyListAdapter;

import java.util.ArrayList;

public class WalletAddressesFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_addresses, null);
        ListView seedsList = (ListView)view.findViewById(R.id.addressesList);
        final ArrayList<String> addresses = new ArrayList<>();
        final TextTouchCopyListAdapter adapter = new TextTouchCopyListAdapter(getActivity(), R.layout.text_touch_copy_list_item, addresses);
        seedsList.setAdapter(adapter);
        Wallet.addresses(new SiaRequest.VolleyCallback(view) {
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
        view.findViewById(R.id.walletCreateCancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                container.setVisibility(View.GONE);
            }
        });
        return view;
    }
}
