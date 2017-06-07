package vandyke.sia.dialogs;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.sia.R;
import vandyke.sia.SiaRequest;
import vandyke.sia.api.Wallet;

public class ReceiveDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_receive_address, null);
        Wallet.newAddress(new SiaRequest.VolleyCallback() {
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

    public static void createAndShow(FragmentManager fragmentManager) {
        new ReceiveDialog().show(fragmentManager, "receive dialog");
    }
}
