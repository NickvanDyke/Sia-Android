/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.api;

import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.view.View;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.misc.SiaMobileApplication;
import vandyke.siamobile.misc.Utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class SiaRequest extends StringRequest {

    private HashMap<String, String> headers;
    private HashMap<String, String> params;

    public SiaRequest(int method, String destination, String command, final VolleyCallback callback) {
        super(method, "http://" + destination + command, new Response.Listener<String>() {
            public void onResponse(String response) {
                try {
                    JSONObject responseJson = response.length() == 0 ? new JSONObject() : new JSONObject(response);
                    callback.onSuccess(responseJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                callback.onError(new Error(error));
            }
        });
        headers = new HashMap<>();
        headers.put("User-agent", "Sia-Agent");
        headers.put("Authorization", "Basic " + Base64.encodeToString((":" + SiaMobileApplication.prefs.getString("apiPass", "")).getBytes(), 0));
        params = new HashMap<>();
    }

    public SiaRequest(int method, String command, final VolleyCallback callback) {
        this(method, SiaMobileApplication.prefs.getString("address", "localhost:9980"), command, callback);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    protected Map<String, String> getParams() {
        return params;
    }

    public SiaRequest addParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public SiaRequest addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public void send() {
        SiaMobileApplication.requestQueue.add(this);
    }

    public interface VolleyCallback {
        void onSuccess(JSONObject response);
        void onError(Error error);
    }

    public static class Error {

        public enum Reason {
            TIMEOUT("Response timed out"),
            NO_NETWORK_RESPONSE("No network response"),
            WALLET_PASSWORD_INCORRECT("Wallet password incorrect"),
            WALLET_LOCKED("Wallet must be unlocked first"),
            WALLET_ALREADY_UNLOCKED("Wallet already unlocked"),
            INVALID_AMOUNT("Invalid amount"),
            NO_NEW_PASSWORD("Must provide new password"),
            WRONG_LENGTH_ADDRESS("Address of invalid length"),
            AMOUNT_ZERO("Amount cannot be zero"),
            INSUFFICIENT_FUNDS("Insufficient funds"),
            EXISTING_WALLET("A wallet already exists. Use force option to overwrite"),
            INCORRECT_API_PASSWORD("Incorrect API password"),
            UNACCOUNTED_FOR_ERROR("Unexpected error"),
            ANOTHER_WALLET_SCAN_UNDERWAY("Wallet scan in progress. Please wait"),
            WALLET_NOT_ENCRYPTED("Wallet has not been encrypted yet"),
            INVALID_WORD_IN_SEED("Invalid word in seed"),
            UNSUPPORTED_ON_COLD_WALLET("Unsupported on cold storage wallet");

            private String msg;

            Reason(String msg) {
                this.msg = msg;
            }

            public String getMsg() {
                return msg;
            }
        }

        private Reason reason;

        /**
         * also attempts to determine what caused the error
         */
        public Error(VolleyError error) {
            if (error instanceof TimeoutError)
                reason = Reason.TIMEOUT;
            else if (error.networkResponse != null) {
                try {
                    String response = new String(error.networkResponse.data, "utf-8");
                    JSONObject responseJson = new JSONObject(response);
                    String errorMessage = responseJson.getString("message");
                    reason = determineReason(errorMessage);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                reason = Reason.NO_NETWORK_RESPONSE;
            }
        }

        public Reason determineReason(String errorMessage) {
            if (errorMessage.contains("wallet must be unlocked before it can be used"))
                return Reason.WALLET_LOCKED;
            else if (errorMessage.contains("provided encryption key is incorrect"))
                return Reason.WALLET_PASSWORD_INCORRECT;
            else if (errorMessage.contains("wallet has already been unlocked"))
                return Reason.WALLET_ALREADY_UNLOCKED;
            else if (errorMessage.contains("could not read 'amount'"))
                return Reason.INVALID_AMOUNT;
            else if (errorMessage.contains("a password must be provided to newpassword"))
                return Reason.NO_NEW_PASSWORD;
            else if (errorMessage.contains("marshalled unlock hash is the wrong length"))
                return Reason.WRONG_LENGTH_ADDRESS;
            else if (errorMessage.contains("transaction cannot have an output or payout that has zero value"))
                return Reason.AMOUNT_ZERO;
            else if (errorMessage.contains("unable to fund transaction"))
                return Reason.INSUFFICIENT_FUNDS;
            else if (errorMessage.contains("wallet is already encrypted, cannot encrypt again"))
                return reason.EXISTING_WALLET;
            else if (errorMessage.contains("API authentication failed"))
                return Reason.INCORRECT_API_PASSWORD;
            else if (errorMessage.contains("another wallet rescan is already underway"))
                return Reason.ANOTHER_WALLET_SCAN_UNDERWAY;
            else if (errorMessage.contains("wallet has not been encrypted yet"))
                return Reason.WALLET_NOT_ENCRYPTED;
            else if (errorMessage.contains("unsupported on cold storage wallet"))
                return Reason.UNSUPPORTED_ON_COLD_WALLET;
            else if (errorMessage.contains("word not found in dictionary for given language"))
                return Reason.INVALID_WORD_IN_SEED;
            else {
                System.out.println(errorMessage);
                return Reason.UNACCOUNTED_FOR_ERROR;
            }
        }

        public Reason getReason() {
            return reason;
        }

        public String getMsg() {
            return reason != null ? reason.getMsg() : "";
        }

        public void snackbar(View view) {
            if (reason != null)
                Utils.snackbar(view, reason.getMsg(), Snackbar.LENGTH_SHORT);
        }
    }

    public static void genericSuccessSnackbar(View view) {
        Utils.snackbar(view, "Success", Snackbar.LENGTH_SHORT);
    }
}
