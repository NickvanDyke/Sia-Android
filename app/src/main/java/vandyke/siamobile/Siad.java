package vandyke.siamobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Siad {

    private static Siad instance;

    private File siad;

    private Process siadProcess;

    private Siad() {
        siad = MainActivity.copyBinary("siad");
        instance = this;
    }

    public static Siad getInstance() {
        if (instance == null)
            instance = new Siad();
        return instance;
    }

    public void start() {
        if (siadProcess != null) {
            System.out.println("siad already running");
            return;
        }
        ProcessBuilder pb = new ProcessBuilder(siad.getAbsolutePath());
        pb.redirectErrorStream(true);
        pb.directory(MainActivity.instance.getFilesDir());
        try {
            siadProcess = pb.start();
            System.out.println(siadProcess);
            StringBuilder stdOut = new StringBuilder();
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(siadProcess.getInputStream()));
            int read;
            char[] buffer = new char[1024];
            while ((read = inputReader.read(buffer)) > 0) {
//                stdOut.append(new String(buffer), 0, read);
                System.out.println(new String(buffer));
            }
            inputReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (siadProcess != null) {
            siadProcess.destroy();
            siadProcess = null;
        }
    }
}
