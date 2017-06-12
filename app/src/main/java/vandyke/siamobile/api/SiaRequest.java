package vandyke.siamobile.api;

import android.util.Base64;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.MainActivity;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class SiaRequest extends StringRequest {

    private HashMap<String, String> headers;
    private HashMap<String, String> params;

    public SiaRequest(int method, String command, final VolleyCallback callback) {
        super(method, "http://" + MainActivity.prefs.getString("address", "10.0.0.2:9980") + command, new Response.Listener<String>() {
            public void onResponse(String response) {
                try {
                    JSONObject responseJson;
                    if (response.length() == 0)
                        responseJson = new JSONObject();
                    else
                        responseJson = new JSONObject(response);
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
        headers.put("Authorization", "Basic " + Base64.encodeToString((":" + MainActivity.prefs.getString("apiPass", "")).getBytes(), 0));
        params = new HashMap<>();
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
        MainActivity.requestQueue.add(this);
    }

    public static class VolleyCallback {
        public void onSuccess(JSONObject response) {
            genericSuccessToast();
        }

        public void onError(Error error) {
            error.toast();
        }
    }

    public static class Error {

        public enum Reason {
            TIMEOUT("Request timed out"),
            NO_NETWORK_RESPONSE("No network response"),
            WALLET_PASSWORD_INCORRECT("Wallet password incorrect"),
            WALLET_LOCKED("Wallet is locked"),
            WALLET_ALREADY_UNLOCKED("Wallet already unlocked"),
            INVALID_AMOUNT("Invalid amount"),
            NO_NEW_PASSWORD("Must provide new password"),
            WRONG_LENGTH_ADDRESS("Address of invalid length"),
            AMOUNT_ZERO("Amount cannot be zero"),
            INSUFFICIENT_FUNDS("Insufficient funds"),
            EXISTING_WALLET("A wallet already exists. Use force option to overwrite"),
            INCORRECT_API_PASSWORD("Incorrect API password"),
            UNACCOUNTED_FOR_ERROR("Unexpected error");

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
                    if (errorMessage.contains("wallet must be unlocked before it can be used"))
                        reason = Reason.WALLET_LOCKED;
                    else if (errorMessage.contains("provided encryption key is incorrect"))
                        reason = Reason.WALLET_PASSWORD_INCORRECT;
                    else if (errorMessage.contains("wallet has already been unlocked"))
                        reason = Reason.WALLET_ALREADY_UNLOCKED;
                    else if (errorMessage.contains("could not read 'amount'"))
                        reason = Reason.INVALID_AMOUNT;
                    else if (errorMessage.contains("a password must be provided to newpassword"))
                        reason = Reason.NO_NEW_PASSWORD;
                    else if (errorMessage.contains("marshalled unlock hash is the wrong length"))
                        reason = Reason.WRONG_LENGTH_ADDRESS;
                    else if (errorMessage.contains("transaction cannot have an output or payout that has zero value"))
                        reason = Reason.AMOUNT_ZERO;
                    else if (errorMessage.contains("unable to fund transaction"))
                        reason = Reason.INSUFFICIENT_FUNDS;
                    else if (errorMessage.contains("wallet is already encrypted, cannot encrypt again"))
                        reason = reason.EXISTING_WALLET;
                    else if (errorMessage.contains("API authentication failed"))
                        reason = Reason.INCORRECT_API_PASSWORD;
                    else {
                        reason = Reason.UNACCOUNTED_FOR_ERROR;
                        System.out.println("ERROR WITH UNCAUGHT REASON");
                    }
                    System.out.println(errorMessage);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                reason = Reason.NO_NETWORK_RESPONSE;
                System.out.println("ERROR WITH NO ACCOMPANYING NETWORKRESPONSE; I don't know what causes this versus timeout");
            }
            System.out.println("ERROR: " + reason);
        }

        public Reason getReason() {
            return reason;
        }

        public String getMsg() {
            return reason.getMsg();
        }

        public void toast() {
            Toast.makeText(MainActivity.instance, reason.getMsg(), Toast.LENGTH_LONG).show();
        }
    }

    public static void genericSuccessToast() {
        Toast.makeText(MainActivity.instance, "Success", Toast.LENGTH_SHORT).show();
    }
}
