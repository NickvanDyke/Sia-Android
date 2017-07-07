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
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

public class DonateDialog extends DialogFragment {

    private String paymentRecipient = MainActivity.devAddresses[(int) (Math.random() * MainActivity.devAddresses.length)];

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = MainActivity.getDialogBuilder(getActivity());

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_donate, null);

        final Button donateButton = (Button) dialogView.findViewById(R.id.donateButton);
        final EditText amount = (EditText) dialogView.findViewById(R.id.donateAmount);

        donateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Wallet.sendSiacoins(Wallet.scToHastings(amount.getText().toString()),
                        paymentRecipient, new SiaRequest.VolleyCallback(view) {
                            public void onSuccess(JSONObject response) {
                                MainActivity.snackbar(dialogView, "Donation successful. Thank you!", Snackbar.LENGTH_SHORT);
                            }

                            public void onError(SiaRequest.Error error) {
                                MainActivity.snackbar(dialogView, error.getMsg() + ". No donation made.", Snackbar.LENGTH_SHORT);
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
