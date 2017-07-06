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
import android.widget.EditText;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

public class WalletChangePasswordDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = MainActivity.getDialogBuilder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_change_password, null);
        builder.setTitle("Change Wallet Password")
                .setView(view)
                .setPositiveButton("Change Password", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String newPassword = ((EditText)view.findViewById(R.id.newPassword)).getText().toString();
                        if (!newPassword.equals(((EditText)view.findViewById(R.id.confirmNewPassword)).getText().toString())) {
                            MainActivity.snackbar(view, "New passwords don't match", Snackbar.LENGTH_SHORT);
                            return; // TODO: make dialog not disappear in this case
                        }
                        Wallet.changePassword(((EditText) view.findViewById(R.id.currentPassword)).getText().toString(),
                                newPassword, new SiaRequest.VolleyCallback(view));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_wallet_change_password, null);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new WalletChangePasswordDialog().show(fragmentManager, "change password dialog");
    }
}
