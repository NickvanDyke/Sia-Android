package vandyke.siamobile.api;

import static com.android.volley.Request.Method.GET;

public class Daemon {

    public static void stop(SiaRequest.VolleyCallback callback) {
        new SiaRequest(GET, "/daemon/stop", callback)
                .send();
    }
}
