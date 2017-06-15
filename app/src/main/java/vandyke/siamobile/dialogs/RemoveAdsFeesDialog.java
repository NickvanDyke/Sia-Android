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

public class RemoveAdsFeesDialog extends DialogFragment {

    private String paymentRecipient = "20c9ed0d1c70ab0d6f694b7795bae2190db6b31d97bc2fba8067a336ffef37aacbc0c826e5d3";
    private String removeAdsCost = "2"; // in SC TODO: actual amounts
    private String removeFeesCost = "2"; //in SC

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_remove_ads_fees, null);

        final Button removeAdsButton = (Button)view.findViewById(R.id.removeAdsButton);
        removeAdsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(getActivity());
                confirmBuilder.setTitle("Confirm")
                        .setMessage("Spend " + removeAdsCost + " Siacoins to remove ads?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Wallet.sendSiacoins(Wallet.scToHastings(removeAdsCost).toPlainString(),
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
                AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(getActivity());
                confirmBuilder.setTitle("Confirm")
                        .setMessage("Spend " + removeFeesCost + " Siacoins to remove app fees? Note that this has no effect on Sia's 0.75 SC miner fee.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Wallet.sendSiacoins(Wallet.scToHastings(removeFeesCost).toPlainString(),
                                        paymentRecipient, new SiaRequest.VolleyCallback() {
                                            public void onSuccess(JSONObject response) {
                                                // TODO: disable fees aside from setting prefs, if necessary
                                                SharedPreferences.Editor editor = MainActivity.prefs.edit();
                                                editor.putBoolean("feesEnabled", false);
                                                editor.apply();
                                                MainActivity.instance.findViewById(R.id.adView).setVisibility(View.GONE);
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
