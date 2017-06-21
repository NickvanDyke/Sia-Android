package vandyke.siamobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Siad {

    private static Siad instance;
    private File siad;
    private Process siadProcess;
    private Thread readStdoutThread;
    final private StringBuilder stdoutBuffer = new StringBuilder();

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
        stdoutBuffer.setLength(0);
        ProcessBuilder pb = new ProcessBuilder(siad.getAbsolutePath(), "-M", "gctw");
        pb.redirectErrorStream(true);
        pb.directory(MainActivity.instance.getFilesDir());
        try {
            siadProcess = pb.start();
            System.out.println(siadProcess);
            readStdoutThread = new Thread() {
                public void run() {
                    try {
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(siadProcess.getInputStream()));
                        int read;
                        char[] buffer = new char[1024];
                        while ((read = inputReader.read(buffer)) > 0) {
                            stdoutBuffer.append(new String(buffer), 0, read);
                            System.out.println(new String(buffer));
                        }
                        inputReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            readStdoutThread.start();
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

    public String getBufferedStdout() {
        if (stdoutBuffer == null)
            return "";
        String result = stdoutBuffer.toString();
        stdoutBuffer.setLength(0);
        return result;
    }
}
