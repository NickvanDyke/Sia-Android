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
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.api.Wallet;

import java.math.BigDecimal;

public class RemoveAdsFeesDialog extends DialogFragment {

    private String paymentRecipient = MainActivity.devAddresses[(int)(Math.random() * MainActivity.devAddresses.length)];
    private BigDecimal removeAdsCost; // in hastings TODO: actual amounts
    private BigDecimal removeFeesCost; //in hastings

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = MainActivity.getDialogBuilder(getActivity());

        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_remove_ads_fees, null);

        final Button removeAdsButton = (Button)view.findViewById(R.id.removeAdsButton);
        final Button removeFeesButton = (Button)view.findViewById(R.id.removeFeesButton);
        final TextView adsCostText = (TextView)view.findViewById(R.id.removeAdsCostText);
        final TextView feesCostText = (TextView)view.findViewById(R.id.removeFeesCostText);

        if (!MainActivity.prefs.getBoolean("adsEnabled", true)) {
            removeAdsButton.setVisibility(View.GONE);
            adsCostText.setVisibility(View.GONE);
        } else {
            Wallet.coincapSC(new Response.Listener() {
                public void onResponse(Object response) {
                    try {
                        JSONObject json = new JSONObject((String) response);
                        double usdPrice = json.getDouble("usdPrice");
                        String cost = Wallet.round(Wallet.usdInSC(usdPrice, "1"));
                        removeAdsCost = Wallet.scToHastings(cost);
                        removeFeesCost = Wallet.scToHastings(cost);
                        adsCostText.setText(cost + " SC");
                        feesCostText.setText(cost + " SC");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getActivity(), "Error retrieving SC/USD. Defaulting to 50 SC", Toast.LENGTH_SHORT).show();
                    removeAdsCost = Wallet.scToHastings("50");
                    removeFeesCost = Wallet.scToHastings("50");
                    adsCostText.setText("50 SC");
                    feesCostText.setText("50 SC");
                }
            });
        }
        if (!MainActivity.prefs.getBoolean("feesEnabled", true)) {
            removeFeesButton.setVisibility(View.GONE);
            feesCostText.setVisibility(View.GONE);
        }

        removeAdsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (removeAdsCost == null) {
                    Toast.makeText(getActivity(), "Please wait, retrieving SC/USD", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder confirmBuilder = MainActivity.getDialogBuilder(getActivity());
                confirmBuilder.setTitle("Confirm")
                        .setMessage("Spend " + Wallet.hastingsToSC(removeAdsCost) + " Siacoins to remove ads?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Wallet.sendSiacoins(Wallet.scToHastings(removeAdsCost),
                                        paymentRecipient, new SiaRequest.VolleyCallback(getActivity()) {
                                            public void onSuccess(JSONObject response) {
                                                SharedPreferences.Editor editor = MainActivity.prefs.edit();
                                                editor.putBoolean("adsEnabled", false);
                                                editor.apply();
                                                // TODO: immediately disappear ads if needed
                                                Toast.makeText(getActivity(), "Success. Ads removed", Toast.LENGTH_SHORT).show();
                                                removeAdsButton.setVisibility(View.GONE);
                                                adsCostText.setVisibility(View.GONE);
                                            }
                                            public void onError(SiaRequest.Error error) {
                                                Toast.makeText(getActivity(), error.getMsg() + ". No funds deducted", Toast.LENGTH_SHORT).show();
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

        removeFeesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (removeFeesCost == null) {
                    Toast.makeText(getActivity(), "Please wait, retrieving SC/USD", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder confirmBuilder = MainActivity.getDialogBuilder(getActivity());
                confirmBuilder.setTitle("Confirm")
                        .setMessage("Spend " + Wallet.hastingsToSC(removeFeesCost) + " Siacoins to remove app fees? Note that this has no effect on the Sia network's miner fees.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Wallet.sendSiacoins(Wallet.scToHastings(removeFeesCost),
                                        paymentRecipient, new SiaRequest.VolleyCallback(getActivity()) {
                                            public void onSuccess(JSONObject response) {
                                                SharedPreferences.Editor editor = MainActivity.prefs.edit();
                                                editor.putBoolean("feesEnabled", false);
                                                editor.apply();
                                                Toast.makeText(adsCostText.getContext(), "Success. App fees removed", Toast.LENGTH_SHORT).show();
                                                removeFeesButton.setVisibility(View.GONE);
                                                feesCostText.setVisibility(View.GONE);
                                            }
                                            public void onError(SiaRequest.Error error) {
                                                Toast.makeText(getActivity(), error.getMsg() + ". No funds deducted", Toast.LENGTH_SHORT).show();
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
