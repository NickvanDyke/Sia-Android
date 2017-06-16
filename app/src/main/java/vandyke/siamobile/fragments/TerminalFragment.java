package vandyke.siamobile.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class TerminalFragment extends Fragment {

    private EditText input;
    private TextView output;
    private File siacFile;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_terminal, container, false);
        MainActivity.instance.getSupportActionBar().setTitle("Terminal");
        copyBinary();

//        Thread socket = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    ServerSocket socket = new ServerSocket(9980);
//                    while (true) {
//                        System.out.println("waiting for connection");
//                        socket.accept();
//                        System.out.println("something connected to socket");
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        socket.start();


        input = (EditText)v.findViewById(R.id.input);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    String enteredCommand = v.getText().toString();
//                    v.setText("");
                    ArrayList<String> fullCommand = new ArrayList<>(Arrays.asList(enteredCommand.split(" ")));
                    fullCommand.add(0, siacFile.getAbsolutePath());
                    ProcessBuilder pb = new ProcessBuilder(fullCommand);
                    pb.redirectErrorStream(true);
                    Process siac = pb.start();
//                    Process siac = Runtime.getRuntime().exec(siacFile.getAbsolutePath() + " " + enteredCommand);

                    SpannableStringBuilder stdOut = new SpannableStringBuilder();
                    stdOut.append("\n" + enteredCommand + "\n");
                    stdOut.setSpan(new ForegroundColorSpan(Color.BLACK), 0, stdOut.length(), 0);
                    stdOut.setSpan(new StyleSpan(Typeface.BOLD), 0, stdOut.length(), 0);

                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(siac.getInputStream()));
                    int read;
                    char[] buffer = new char[1024];
                    while ((read = inputReader.read(buffer)) > 0) {
                        stdOut.append(new String(buffer), 0, read);
                    }
                    inputReader.close();
//                    stdOut.append("\n");

                    stdOut.setSpan(new ForegroundColorSpan(Color.GRAY), enteredCommand.length() + 2, stdOut.length(), 0);
                    output.append(stdOut);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        output = (TextView)v.findViewById(R.id.output);
        output.setMovementMethod(new ScrollingMovementMethod());
        return v;
    }

    /**
     * Copies siac from the assets folder of the app to a location on the device that it can be executed from
     */
    private void copyBinary() {
        try {
            InputStream in = getContext().getAssets().open("siac");
            siacFile = new File(getContext().getFilesDir().getPath() + "/siac");
            if (siacFile.exists())
                return;
            FileOutputStream out = new FileOutputStream(siacFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
            in.close();
            out.close();
            siacFile.setExecutable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
