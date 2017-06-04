package vandyke.sia.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import org.json.JSONObject;
import vandyke.sia.R;
import vandyke.sia.SiaRequest;

public class SendDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_send, null);
        builder.setTitle("Receive Address")
                .setView(view)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SiaRequest.sendSiacoins(((EditText) view.findViewById(R.id.sendRecipient)).getText().toString(),
                                ((EditText) view.findViewById(R.id.sendAmount)).getText().toString(),
                                new SiaRequest.VolleyCallback() {
                                    public void onSuccess(JSONObject response) {
                                        System.out.println(response);
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

    public static void createAndShow(FragmentManager fragmentManager) {
        new SendDialog().show(fragmentManager, "receive dialog");
    }
}
