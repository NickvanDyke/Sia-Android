package vandyke.siamobile;

import java.io.File;
import java.io.IOException;

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
        try {
            siadProcess = pb.start();
            System.out.println(siadProcess);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        siadProcess.destroy();
    }
}
