package vandyke.siamobile.api;

import vandyke.siamobile.SiaRequest;

import static com.android.volley.Request.Method.GET;

public class Consensus {

    public static void consensus(SiaRequest.VolleyCallback callback) {
        new SiaRequest(GET, "/consensus", callback)
                .send();
    }
}
