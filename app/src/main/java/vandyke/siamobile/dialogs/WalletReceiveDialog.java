/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

public class WalletReceiveDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = MainActivity.getDialogBuilder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_receive, null);
        if (MainActivity.theme == MainActivity.Theme.CUSTOM)
            ((TextView)view.findViewById(R.id.receiveAddress)).setTextColor(Color.GRAY);
        Wallet.newAddress(new SiaRequest.VolleyCallback(view) {
            public void onSuccess(JSONObject response) {
                try {
                    ((TextView)view.findViewById(R.id.receiveAddress)).setText(response.getString("address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setTitle("Receive Address")
                .setView(view)
                .setPositiveButton("Copy", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("receive address", ((TextView)view.findViewById(R.id.receiveAddress)).getText());
                        clipboard.setPrimaryClip(clip);
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_wallet_receive, null);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new WalletReceiveDialog().show(fragmentManager, "receive dialog");
    }
}
