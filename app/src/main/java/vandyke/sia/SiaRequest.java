package vandyke.sia;

import android.util.Base64;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class SiaRequest extends StringRequest {

    private HashMap<String, String> params;

    public SiaRequest(int method, String command, final VolleyCallback callback) {
        super(method, "http://" + MainActivity.prefs.getString("address", "10.0.0.2:9980") + command, new Response.Listener<String>() {
            public void onResponse(String response) {
                callback.onResponse(response);
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                try {
                    System.out.println(new String(error.networkResponse.data, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        params = new HashMap<>();
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-agent", "Sia-Agent");
        headers.put("Authorization", "Basic " + Base64.encodeToString((":" + MainActivity.prefs.getString("apiPass", "")).getBytes(), 0));
        return headers;
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    public void send() {
        MainActivity.requestQueue.add(this);
    }

    public interface VolleyCallback {
        void onResponse(String response);
    }
}
