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
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.misc.Utils;

public class WalletUnlockDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = Utils.getDialogBuilder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_unlock, null);
        builder.setTitle("Unlock Wallet")
                .setView(view)
                .setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Wallet.unlock(((EditText)view.findViewById(R.id.walletPassword)).getText().toString(), new SiaRequest.VolleyCallback(view));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_wallet_unlock, null);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new WalletUnlockDialog().show(fragmentManager, "unlock wallet dialog");
    }
}
