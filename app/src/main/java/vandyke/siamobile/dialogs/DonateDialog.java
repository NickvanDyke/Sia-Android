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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import org.json.JSONObject;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.misc.Utils;

public class DonateDialog extends DialogFragment {

    private String paymentRecipient = Utils.devAddresses[(int) (Math.random() * Utils.devAddresses.length)];

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = Utils.getDialogBuilder(getActivity());

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_donate, null);

        final Button donateButton = (Button) dialogView.findViewById(R.id.donateButton);
        final EditText amount = (EditText) dialogView.findViewById(R.id.donateAmount);

        donateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Wallet.sendSiacoins(Wallet.scToHastings(amount.getText().toString()),
                        paymentRecipient, new SiaRequest.VolleyCallback() {
                            public void onSuccess(JSONObject response) {
                                Utils.snackbar(dialogView, "Donation successful. Thank you!", Snackbar.LENGTH_SHORT);
                            }

                            public void onError(SiaRequest.Error error) {
                                Utils.snackbar(dialogView, error.getMsg() + ". No donation made.", Snackbar.LENGTH_SHORT);
                            }
                        });
            }
        });
        builder.setTitle("Donate")
                .setView(dialogView)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_donate, null);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new DonateDialog().show(fragmentManager, "donate dialog");
    }
}
