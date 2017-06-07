package vandyke.sia.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import org.json.JSONObject;
import vandyke.sia.R;
import vandyke.sia.SiaRequest;
import vandyke.sia.api.Wallet;

public class SendDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_send, null);
        builder.setTitle("Send Siacoins")
                .setView(view)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Wallet.sendSiacoins(((EditText)view.findViewById(R.id.sendRecipient)).getText().toString(),
                                Wallet.scToHastings(((EditText)view.findViewById(R.id.sendAmount)).getText().toString()).toString(),
                                new SiaRequest.VolleyCallback() {
                                    public void onSuccess(JSONObject response) {
                                        System.out.println(response);
                                    }
                                    public void onError(JSONObject error) {
                                        SiaRequest.checkIfWalletLocked(getContext(), error);
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_send, null);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new SendDialog().show(fragmentManager, "receive dialog");
    }
}
