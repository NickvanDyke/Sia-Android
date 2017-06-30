package vandyke.siamobile;

import android.app.Activity;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class LocalWallet extends NanoHTTPD {

    private static LocalWallet instance;

    private String seed;
    private ArrayList<String> addresses;

    private File binary;

    private LocalWallet(Activity activity) {
        super("localhost", 9980);
        seed = MainActivity.prefs.getString("localWalletSeed", "noseed");
        addresses = new ArrayList<>(MainActivity.prefs.getStringSet("localWalletAddresses", new HashSet<String>()));
        binary = MainActivity.copyBinary("sia-coldstorage", activity ,true);
    }

    public static LocalWallet getInstance(Activity activity) {
        if (instance == null)
            instance = new LocalWallet(activity);
        return instance;
    }

    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        JSONObject response = new JSONObject();
        Response.Status status = Response.Status.OK;
        System.out.println(uri);
        try {
            if (uri.contains("/wallet/addresses")) {
                JSONArray addressArray = new JSONArray();
                for (String address : addresses)
                    addressArray.put(address);
                response.put("addresses", addressArray);
            } else if (uri.contains("/wallet/address")) {
                response.put("address", addresses.get((int)(Math.random() * addresses.size())));
            } else if (uri.contains("/wallet/seeds")) {
                JSONArray seedsArray = new JSONArray();
                seedsArray.put(seed);
                response.put("allseeds", seedsArray);
            } else if (uri.contains("/wallet/init")) {
                newWallet();
                response.put("primaryseed", seed);
            } else {
                response.put("message", "unsupported on cold storage wallet");
                status = Response.Status.NOT_IMPLEMENTED;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Response httpResponse = newFixedLengthResponse(response.toString());
        httpResponse.setStatus(status);
        return httpResponse;
    }

    public static void destroy() {
        if (instance == null)
            return;
        instance.stop();
        instance = null;
    }

    public void newWallet() {
        try {
            ArrayList<String> fullCommand = new ArrayList<>();
            fullCommand.add(0, binary.getAbsolutePath());
            ProcessBuilder pb = new ProcessBuilder(fullCommand);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder stdOut = new StringBuilder();
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[1024];
            while ((read = inputReader.read(buffer)) > 0) {
                stdOut.append(new String(buffer), 0, read);
            }
            inputReader.close();
            JSONObject json = new JSONObject(stdOut.toString());
            seed = json.getString("Seed");
            JSONArray addressesJson = json.getJSONArray("Addresses");
            addresses.clear();
            for (int i = 0; i < addressesJson.length(); i++)
                addresses.add(addressesJson.getString(i).trim());
            System.out.println(seed);
            System.out.println(addresses);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
