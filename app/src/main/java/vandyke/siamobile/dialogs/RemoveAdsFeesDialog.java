package vandyke.siamobile.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

import java.math.BigDecimal;

public class RemoveAdsFeesDialog extends DialogFragment {

    private String paymentRecipient = MainActivity.devAddresses[(int)(Math.random() * MainActivity.devAddresses.length)];
    private BigDecimal removeAdsCost = new BigDecimal("100"); // in SC TODO: actual amounts
    private BigDecimal removeFeesCost = new BigDecimal("100"); //in SC

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = MainActivity.getDialogBuilder();

        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_remove_ads_fees, null);

        final Button removeAdsButton = (Button)view.findViewById(R.id.removeAdsButton);
        removeAdsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder confirmBuilder = MainActivity.getDialogBuilder();
                confirmBuilder.setTitle("Confirm")
                        .setMessage("Spend " + removeAdsCost + " Siacoins to remove ads?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Wallet.sendSiacoins(Wallet.scToHastings(removeAdsCost),
                                        paymentRecipient, new SiaRequest.VolleyCallback() {
                                            public void onSuccess(JSONObject response) {
                                                SharedPreferences.Editor editor = MainActivity.prefs.edit();
                                                editor.putBoolean("adsEnabled", false);
                                                editor.apply();
                                                MainActivity.instance.findViewById(R.id.adView).setVisibility(View.GONE);
                                                Toast.makeText(MainActivity.instance, "Success. Ads removed", Toast.LENGTH_SHORT).show();
                                            }
                                            public void onError(SiaRequest.Error error) {
                                                Toast.makeText(MainActivity.instance, error.getMsg() + ". No funds deducted", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                confirmBuilder.show();
            }
        });

        final Button removeFeesButton = (Button)view.findViewById(R.id.removeFeesButton);
        removeFeesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder confirmBuilder = MainActivity.getDialogBuilder();
                confirmBuilder.setTitle("Confirm")
                        .setMessage("Spend " + removeFeesCost + " Siacoins to remove app fees? Note that this has no effect on the Sia network's miner fees.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Wallet.sendSiacoins(Wallet.scToHastings(removeFeesCost),
                                        paymentRecipient, new SiaRequest.VolleyCallback() {
                                            public void onSuccess(JSONObject response) {
                                                SharedPreferences.Editor editor = MainActivity.prefs.edit();
                                                editor.putBoolean("feesEnabled", false);
                                                editor.apply();
                                                Toast.makeText(MainActivity.instance, "Success. App fees removed", Toast.LENGTH_SHORT).show();
                                            }
                                            public void onError(SiaRequest.Error error) {
                                                Toast.makeText(MainActivity.instance, error.getMsg() + ". No funds deducted", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                confirmBuilder.show();
            }
        });

        builder.setTitle("Remove Ads/Fees")
                .setView(view)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        
                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_remove_ads_fees, null);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new RemoveAdsFeesDialog().show(fragmentManager, "remove ads/fees dialog");
    }
}
