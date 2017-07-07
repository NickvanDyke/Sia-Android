/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.api;

import static com.android.volley.Request.Method.GET;

public class Daemon {

    public static void stop(SiaRequest.VolleyCallback callback) {
        new SiaRequest(GET, "/daemon/stop", callback)
                .send();
    }
}
