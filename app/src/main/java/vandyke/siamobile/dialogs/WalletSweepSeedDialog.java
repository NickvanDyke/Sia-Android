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
import android.view.WindowManager;
import android.widget.EditText;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

public class WalletSweepSeedDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        if (MainActivity.darkMode)
            builder = new AlertDialog.Builder(getActivity(), R.style.DarkDialogTheme);
        else
            builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_sweep, null);
        builder.setTitle("Sweep Seed")
                .setView(view)
                .setPositiveButton("Sweep", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Wallet.sweepSeed("english", ((EditText)view.findViewById(R.id.walletSweepSeed)).getText().toString(),
                                new SiaRequest.VolleyCallback());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_wallet_sweep, null);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new WalletSweepSeedDialog().show(fragmentManager, "wallet init dialog");
    }
}
