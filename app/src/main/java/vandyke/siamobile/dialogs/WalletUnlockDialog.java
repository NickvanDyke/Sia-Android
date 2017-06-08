package vandyke.siamobile.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import vandyke.siamobile.R;
import vandyke.siamobile.SiaRequest;
import vandyke.siamobile.api.Wallet;

public class WalletUnlockDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_unlock, null);
        builder.setTitle("Unlock Wallet")
                .setView(view)
                .setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Wallet.unlock(((EditText)view.findViewById(R.id.walletPassword)).getText().toString(), new SiaRequest.VolleyCallback());
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
