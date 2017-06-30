package vandyke.siamobile.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

public class WalletCreateDialog extends DialogFragment {
    
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = MainActivity.getDialogBuilder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_create, null);

        final CheckBox createFromSeed = (CheckBox) view.findViewById(R.id.walletCreateFromSeed);
        final EditText seedField = (EditText) view.findViewById(R.id.walletCreateSeed);
        seedField.setVisibility(View.GONE);
        createFromSeed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (createFromSeed.isChecked())
                    seedField.setVisibility(View.VISIBLE);
                else
                    seedField.setVisibility(View.GONE);
            }
        });

        final CheckBox forceCheck = (CheckBox) view.findViewById(R.id.walletCreateForce);
        final TextView forceWarning = (TextView) view.findViewById(R.id.walletCreateForceWarning);
        forceWarning.setVisibility(View.GONE);
        forceCheck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (forceCheck.isChecked())
                    forceWarning.setVisibility(View.VISIBLE);
                else
                    forceWarning.setVisibility(View.GONE);
            }
        });

        builder.setTitle("Create Wallet")
                .setView(view)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String password = ((EditText) view.findViewById(R.id.newPasswordCreate)).getText().toString();
                        if (!password.equals(((EditText) view.findViewById(R.id.confirmNewPasswordCreate)).getText().toString())) {
                            Toast.makeText(getActivity(), "New passwords don't match", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        boolean force = forceCheck.isChecked();
                        String dictionary = "english";
                        if (createFromSeed.isChecked())
                            Wallet.initSeed(password, force, dictionary, seedField.getText().toString(), new SiaRequest.VolleyCallback(getActivity()) {
                                public void onError(SiaRequest.Error error) {
                                    if (error.getReason() == SiaRequest.Error.Reason.WALLET_NOT_ENCRYPTED)
                                        Toast.makeText(getActivity(), "Success. Scanning blockchain, please wait", Toast.LENGTH_SHORT).show();
                                    else
                                        super.onError(error);
                                }
                            });
                        else
                            Wallet.init(password, force, dictionary, new SiaRequest.VolleyCallback(getActivity()));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_wallet_create, null);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new WalletCreateDialog().show(fragmentManager, "wallet init dialog");
    }
}
