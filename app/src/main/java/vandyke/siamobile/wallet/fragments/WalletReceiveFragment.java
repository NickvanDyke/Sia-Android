/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.fragments;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import net.glxn.qrgen.android.QRCode;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.misc.Utils;

public class WalletReceiveFragment extends Fragment {

    ImageView qrImageView;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet_receive, null);
        final TextView address = (TextView)view.findViewById(R.id.receiveAddress);
        qrImageView = (ImageView) view.findViewById(R.id.walletQrCode);
        qrImageView.setVisibility(View.INVISIBLE);

        Wallet.newAddress(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                try {
                    Utils.successSnackbar(view);
                    address.setText(response.getString("address"));
                    setQrCode(response.getString("address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onError(SiaRequest.Error error) {
                error.snackbar(view);
                address.setText(error.getMsg() + "\n");
            }
        });
        view.findViewById(R.id.walletAddressCopy).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("receive address", ((TextView)view.findViewById(R.id.receiveAddress)).getText());
                clipboard.setPrimaryClip(clip);
                Utils.snackbar(view, "Copied receive address", Snackbar.LENGTH_SHORT);
                container.setVisibility(View.GONE);
            }
        });
        view.findViewById(R.id.walletAddressClose).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                container.setVisibility(View.GONE);
            }
        });

        return view;
    }

    public void setQrCode(String walletAddress) {
        qrImageView.setVisibility(View.VISIBLE);
        qrImageView.setImageBitmap(QRCode.from(walletAddress).bitmap());

    }
}
