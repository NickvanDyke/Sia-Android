/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.coldstorage;

import android.content.Context;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import siawallet.Wallet;
import vandyke.siamobile.SiaMobileApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class ColdStorageHttpServer extends NanoHTTPD {

    private String seed;
    private ArrayList<String> addresses;
    private String password;
    private boolean exists;
    private boolean unlocked;

    private Context context;

    public ColdStorageHttpServer(Context context) {
        super("localhost", 9990);
        seed = SiaMobileApplication.prefs.getString("coldStorageSeed", "noseed");
        addresses = new ArrayList<>(SiaMobileApplication.prefs.getStringSet("coldStorageAddresses", new HashSet<String>()));
        password = SiaMobileApplication.prefs.getString("coldStoragePassword", "nopass");
        exists = SiaMobileApplication.prefs.getBoolean("coldStorageExists", false);
        unlocked = false;
        this.context = context;
    }

    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        JSONObject response = new JSONObject();
        Response.Status status = Response.Status.OK;
        Map<String, String> parms = session.getParms();
        try {
            session.parseBody(parms);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        try {
            switch (uri) {
                case "/wallet/addresses":
                    if (checkExists(response) && checkUnlocked(response)) {
                        JSONArray addressArray = new JSONArray();
                        for (String address : addresses)
                            addressArray.put(address);
                        response.put("addresses", addressArray);
                    } else
                        status = Response.Status.BAD_REQUEST;
                    break;
                case "/wallet/address":
                    if (checkExists(response) && checkUnlocked(response)) {
                        response.put("address", addresses.get((int) (Math.random() * addresses.size())));
                    } else
                        status = Response.Status.BAD_REQUEST;
                    break;
                case "/wallet/seeds":
                    if (checkExists(response) && checkUnlocked(response)) {
                        JSONArray seedsArray = new JSONArray();
                        seedsArray.put(seed);
                        response.put("allseeds", seedsArray);
                    } else
                        status = Response.Status.BAD_REQUEST;
                    break;
                case "/wallet/init":
                    if (!exists || parms.get("force").equals("true")) {
                        newWallet(parms.get("encryptionpassword"));
                        response.put("primaryseed", seed);
                    } else {
                        response.put("message", "wallet is already encrypted, cannot encrypt again");
                        status = Response.Status.BAD_REQUEST;
                    }
                    break;
                case "/wallet/unlock":
                    if (checkExists(response) && parms.get("encryptionpassword").equals(password)) {
                        unlocked = true;
                    } else {
                        status = Response.Status.BAD_REQUEST;
                        response.put("message", "provided encryption key is incorrect");
                    }
                    break;
                case "/wallet/lock":
                    if (checkExists(response))
                        unlocked = false;
                    else
                        status = Response.Status.BAD_REQUEST;
                    break;
                case "/wallet":
                    response.put("encrypted", exists);
                    response.put("unlocked", unlocked);
                    response.put("rescanning", false);
                    response.put("confirmedsiacoinbalance", 0);
                    response.put("unconfirmedoutgoingsiacoins", 0);
                    response.put("unconfirmedincomingsiacoins", 0);
                    response.put("siafundbalance", 0);
                    response.put("siacoinclaimbalance", 0);
                    break;
                case "/wallet/transactions":
                    break;
                case "/consensus":
                    response.put("synced", true);
                    response.put("height", 0);
                    break;
                default:
                    response.put("message", "unsupported on cold storage wallet");
                    status = Response.Status.NOT_IMPLEMENTED;
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Response httpResponse = newFixedLengthResponse(response.toString());
        httpResponse.setStatus(status);
        return httpResponse;
    }

    private boolean checkUnlocked(JSONObject response) throws JSONException {
        if (!unlocked) {
            response.put("message", "wallet must be unlocked before it can be used");
        }
        return unlocked;
    }

    private boolean checkExists(JSONObject response) throws JSONException {
        if (!exists) {
            response.put("message", "wallet has not been encrypted yet");
        }
        return exists;
    }

    public void newWallet(String password) {
        Wallet wallet = new Wallet();
        try {
            wallet.generateSeed();
            seed = wallet.getSeed();
        } catch (Exception e) {
            e.printStackTrace();
            seed = "Failed to generate seed";
        }

        addresses.clear();
        for (int i = 0; i < 20; i++)
            addresses.add(wallet.getAddress(i));

        this.password = password;
        exists = true;
        unlocked = false;
        SiaMobileApplication.prefs.edit()
                .putString("coldStorageSeed", seed)
                .putStringSet("coldStorageAddresses", new HashSet<>(addresses))
                .putString("coldStoragePassword", password)
                .putBoolean("coldStorageExists", true)
                .apply();
    }
}
