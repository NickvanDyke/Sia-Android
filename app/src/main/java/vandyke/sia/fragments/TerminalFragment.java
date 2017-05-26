package vandyke.sia.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import vandyke.sia.R;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class TerminalFragment extends Fragment {

    private EditText input;
    private TextView output;
    private File siacFile;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_terminal, container, false);

        copyBinary();

        input = (EditText)v.findViewById(R.id.input);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    System.out.println("Enter pressed");
                    ArrayList<String> command = new ArrayList<>(Arrays.asList(v.getText().toString().split(" ")));
                    command.add(0, siacFile.getAbsolutePath());
                    ProcessBuilder pb = new ProcessBuilder(command);
                    pb.redirectErrorStream(true);
                    Process siac = pb.start();

                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(siac.getInputStream()));
                    int read;
                    char[] buffer = new char[1024];
                    StringBuilder stdOut = new StringBuilder();
                    while ((read = inputReader.read(buffer)) > 0) {
                        stdOut.append(buffer, 0, read);
                    }
                    inputReader.close();

//                    siac.waitFor();

                    System.out.println(stdOut.toString());
                    output.append(stdOut.toString());
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
