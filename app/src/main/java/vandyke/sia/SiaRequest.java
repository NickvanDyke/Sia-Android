package vandyke.sia;

import android.util.Base64;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;

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
                        System.out.println(new String(error.networkResponse.data, "utf-8"));
//                        if (json.getString("message").contains("wallet must be unlocked before it can be used"))
                    }
                } catch (UnsupportedEncodingException e) {
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

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void send() {
        MainActivity.requestQueue.add(this);
    }

    public interface VolleyCallback {
        void onSuccess(JSONObject response);
//        void onError(JSONObject error);
    }


    /** API Methods for convenience */

    public static void wallet(VolleyCallback callback) {
        new SiaRequest(Request.Method.GET, "/wallet", callback).send();
    }

    public static void walletUnlock(String password, VolleyCallback callback) {
        SiaRequest request = new SiaRequest(Method.POST, "/wallet/unlock", callback);
        request.addParam("encryptionpassword", password);
        request.send();
    }

    public static void walletLock(VolleyCallback callback) {
        new SiaRequest(Method.POST, "/wallet/lock", callback).send();
    }

    public static void walletAddress(VolleyCallback callback) {
        new SiaRequest(Method.GET, "/wallet/address", callback).send();
    }

    public static void walletAddresses(VolleyCallback callback) {
        new SiaRequest(Method.GET, "/wallet/addresses", callback).send();
    }

    public static void sendSiacoins(String recipient, String amount, VolleyCallback callback) { // TODO: actual value sent isn't what's entered?
        SiaRequest request = new SiaRequest(Method.POST, "/wallet/siacoins", callback);
        request.addParam("amount", amount);
        request.addParam("destination", recipient);
        request.send();
    }

    public static void transactions(VolleyCallback callback) {
        // TODO: maybe use actual value instead of really big literal lol
        SiaRequest request = new SiaRequest(Method.GET, String.format("/wallet/transactions?startheight=%s&endheight=%s", "0", "1000000000"), callback);
        request.send();
    }

    public static BigDecimal hastingsToSC(String hastings) {
        return new BigDecimal(hastings).divide(new BigDecimal("1000000000000000000000000"));
    }

    public static BigDecimal hastingsToSC(BigDecimal hastings) {
        return hastings.divide(new BigDecimal("1000000000000000000000000"));
    }

    public static BigDecimal scToHastings(String sc) {
        return new BigDecimal(sc).multiply(new BigDecimal("1000000000000000000000000"));
    }

    public static BigDecimal scToHastings(BigDecimal sc) {
        return sc.multiply(new BigDecimal("1000000000000000000000000"));
    }
}
