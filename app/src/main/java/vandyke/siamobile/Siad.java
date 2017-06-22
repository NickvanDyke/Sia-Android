package vandyke.siamobile;

import vandyke.siamobile.fragments.TerminalFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Siad {

    private static Siad instance;
    private File siadFile;
    private Process siadProcess;
    private Thread readStdoutThread;
    final private StringBuilder stdoutBuffer = new StringBuilder();
    private TerminalFragment terminalFragment;

    private Siad() {
        siadFile = MainActivity.copyBinary("siad");
        instance = this;
    }

    public static Siad getInstance() {
        if (instance == null)
            instance = new Siad();
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void start() {
        if (siadProcess != null) {
            System.out.println("siadFile already running");
            return;
        }
        stdoutBuffer.setLength(0);
        terminalAppend("\nStarting siadFile...\n");
        ProcessBuilder pb = new ProcessBuilder(siadFile.getAbsolutePath(), "-M", "gctw");
        pb.redirectErrorStream(true);
        pb.directory(MainActivity.getWorkingDirectory());
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
                            final String text = new String(buffer).substring(0, read);
                            System.out.println(text);
                            MainActivity.instance.runOnUiThread(new Runnable() {
                                public void run() {
                                    terminalAppend(text);
                                }
                            });
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
        destroyInstance();
        terminalAppend("Stopped siadFile\n");
    }

    public String getBufferedStdout() {
        if (stdoutBuffer == null)
            return "";
        String result = stdoutBuffer.toString();
        stdoutBuffer.setLength(0);
        return result;
    }

    public void setTerminalFragment(TerminalFragment fragment) {
        terminalFragment = fragment;
    }

    private void terminalAppend(String text) {
        if (terminalFragment != null)
            terminalFragment.appendToOutput(text);
        else
            stdoutBuffer.append(text);
    }
}
