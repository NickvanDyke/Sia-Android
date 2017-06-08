package vandyke.siamobile;

import android.util.Base64;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;

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
                    callback.onSuccess(new JSONObject(response));
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

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
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
        enum Reason {
            TIMEOUT,
            WALLET_PASSWORD_INCORRECT,
            WALLET_LOCKED,
            UNACCOUNTED_FOR_ERROR
        }
        private Reason reason;
        /** also determines what caused the error */
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
                    else {
                        reason = Reason.UNACCOUNTED_FOR_ERROR;
                        System.out.println("ERROR WITH UNCAUGHT REASON: " + errorMessage);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("ERROR: " + reason);
        }

        public Reason getReason() {
            return reason;
        }

        public void toast() {
            String msg = "";
            switch (reason) {
                case TIMEOUT:
                    msg = "Request timed out";
                    break;
                case WALLET_PASSWORD_INCORRECT:
                    msg = "Wallet password incorrect";
                    break;
                case WALLET_LOCKED:
                    msg = "Wallet is locked";
                    break;
                case UNACCOUNTED_FOR_ERROR:
                    msg = "Unexpected error";
                    break;
            }
            Toast.makeText(MainActivity.instance, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public static void genericSuccessToast() {
        Toast.makeText(MainActivity.instance, "Success", Toast.LENGTH_SHORT).show();
    }
}
