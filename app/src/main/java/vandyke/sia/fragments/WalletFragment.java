package vandyke.sia.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import vandyke.sia.MainActivity;
import vandyke.sia.R;

import java.util.HashMap;
import java.util.Map;

public class WalletFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);

        StringRequest request = new StringRequest(Request.Method.GET, "http://" + MainActivity.prefs.getString("address", "10.0.0.2:9980") + "/wallet",
                new Response.Listener<String>() {
                    public void onResponse(String response) {
                        System.out.println(response);
                    }
                }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new HashMap<>();
                headers.put("User-agent", "Sia-Agent");
                headers.put("Authorization", "Basic " + Base64.encodeToString(MainActivity.prefs.getString("apiPass", "").getBytes(), 0));
                return headers;
            }
        };

        MainActivity.requestQueue.add(request);

        return v;
    }
}
