package vandyke.siamobile.fragments;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class TerminalFragment extends Fragment {

    private EditText input;
    private TextView output;
    private File siacFile;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_terminal, container, false);
        MainActivity.instance.getSupportActionBar().setTitle("Terminal");
        siacFile = MainActivity.copyBinary("siac");

        input = (EditText)v.findViewById(R.id.input);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    String enteredCommand = v.getText().toString();
                    v.setText("");
                    ArrayList<String> fullCommand = new ArrayList<>(Arrays.asList(enteredCommand.split(" ")));
                    fullCommand.add(0, siacFile.getAbsolutePath());
                    ProcessBuilder pb = new ProcessBuilder(fullCommand);
                    pb.redirectErrorStream(true);
                    Process siac = pb.start();

                    SpannableStringBuilder stdOut = new SpannableStringBuilder();
                    stdOut.append("\n" + enteredCommand + "\n");
//                    stdOut.setSpan(new ForegroundColorSpan(Color.BLACK), 0, stdOut.length(), 0);
                    stdOut.setSpan(new StyleSpan(Typeface.BOLD), 0, stdOut.length(), 0);

                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(siac.getInputStream()));
                    int read;
                    char[] buffer = new char[1024];
                    while ((read = inputReader.read(buffer)) > 0) {
                        stdOut.append(new String(buffer), 0, read);
                    }
                    inputReader.close();

                    stdOut.setSpan(new ForegroundColorSpan(MainActivity.defaultTextColor), enteredCommand.length() + 2, stdOut.length(), 0);
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
}
