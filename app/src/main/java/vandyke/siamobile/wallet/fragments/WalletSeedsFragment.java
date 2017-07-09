/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

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

public class WalletSeedsFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_wallet_seeds, null);
        ListView seedsList = (ListView) view.findViewById(R.id.seedsList);
        final ArrayList<String> seeds = new ArrayList<>();
        final TextTouchCopyListAdapter adapter = new TextTouchCopyListAdapter(getActivity(), R.layout.text_touch_copy_list_item, seeds);
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
            public void onError(SiaRequest.Error error) {
                error.snackbar(view);
            }
        });
        view.findViewById(R.id.walletSeedsClose).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                container.setVisibility(View.GONE);
            }
        });
        return view;
    }
}
