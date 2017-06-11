package vandyke.siamobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class LocalWallet {

    private String seed;
    private HashSet<String> addresses;
    
    private File binary;

    public LocalWallet() {
        seed = MainActivity.prefs.getString("localWalletSeed", "noseed");
        addresses = (HashSet<String>)MainActivity.prefs.getStringSet("localWalletAddresses", new HashSet<String>());
        copyBinary();
    }

    public void startListening(String ipPort) {

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

    private void copyBinary() {
        try {
            InputStream in = MainActivity.instance.getAssets().open("sia-coldstorage");
            binary = new File(MainActivity.instance.getFilesDir(), "sia-coldstorage");
            if (binary.exists())
                return;
            FileOutputStream out = new FileOutputStream(binary);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
