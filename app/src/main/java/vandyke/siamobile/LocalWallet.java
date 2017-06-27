package vandyke.siamobile;

import android.app.Activity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

public class LocalWallet {

    private static LocalWallet instance;

    private String seed;
    private HashSet<String> addresses;

    private ServerSocket socket;
    private Thread socketThread;

    private File binary;

    private LocalWallet(Activity activity) {
        seed = MainActivity.prefs.getString("localWalletSeed", "noseed");
        addresses = (HashSet<String>)MainActivity.prefs.getStringSet("localWalletAddresses", new HashSet<String>());
        binary = MainActivity.copyBinary("sia-coldstorage-arm", activity);
    }

    public static LocalWallet getInstance(Activity activity) {
        if (instance == null)
            instance = new LocalWallet(activity);
        return instance;
    }

    public void startListening(final int port) {
        if (socketThread != null || socketThread.isAlive()) {
            System.out.println("localwallet is already listening");
            return;
        }
        try {
            socket = new ServerSocket(port);
            socketThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (true) {
                            System.out.println("waiting for connection");
                            Socket client = socket.accept();
                            System.out.println("something connected to socket");
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            PrintWriter out = new PrintWriter(client.getOutputStream());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            socketThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            // parse the output
            JSONObject json = new JSONObject(stdOut.toString());
            seed = json.getString("Seed");
            JSONArray addressesJson = json.getJSONArray("Addresses");
            addresses.clear();
            for (int i = 0; i < addressesJson.length(); i++)
                addresses.add(addressesJson.getString(i));
            System.out.println(seed);
            System.out.println(addresses);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
