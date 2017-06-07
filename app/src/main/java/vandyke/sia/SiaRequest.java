package vandyke.sia;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.sia.dialogs.UnlockWalletDialog;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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
                try {
                    if (error.networkResponse != null) {
                        String response = new String(error.networkResponse.data, "utf-8");
                        System.out.println(response);
                        callback.onError(new JSONObject(response));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    public interface VolleyCallback {
        void onSuccess(JSONObject response);
        void onError(JSONObject error);
    }


    /** methods for checking for specific error messages from Volley error responses and taking appropriate action */

    public static void checkIfWalletLocked(Context context, JSONObject json) {
        try { // TODO: get proper message string
            if (json.getString("message").contains("wallet must be unlocked"))
                Toast.makeText(context, "Wallet locked", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void checkIfIncorrectWalletPassword(Context context, JSONObject json) {
        try { // TODO: get proper message string
            if (json.getString("message").contains("wrong password"))
                Toast.makeText(context, "Incorrect wallet password", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void genericSuccessToast(Context context) {
        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
    }
}
