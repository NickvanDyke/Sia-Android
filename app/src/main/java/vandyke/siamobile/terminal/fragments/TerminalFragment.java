/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.terminal.fragments;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import vandyke.siamobile.backend.Siad;

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
        siacFile = MainActivity.copyBinary("siac", getActivity(), true);

        input = (EditText)v.findViewById(R.id.input);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    if (siacFile == null) {
                        output.append("\nYour device's CPU architecture is not supported by siac. Sorry! There's nothing Sia Mobile can do about this\n");
                        return true;
                    }
                    final String enteredCommand = v.getText().toString();
                    v.setText("");
                    ArrayList<String> fullCommand = new ArrayList<>(Arrays.asList(enteredCommand.split(" ")));
                    fullCommand.add(0, siacFile.getAbsolutePath());
                    ProcessBuilder pb = new ProcessBuilder(fullCommand);
                    pb.redirectErrorStream(true);
                    final Process siac = pb.start();

                    new Thread() {
                        public void run() {
                            try {
                                final SpannableStringBuilder stdOut = new SpannableStringBuilder();
                                stdOut.append("\n" + enteredCommand + "\n");
                                stdOut.setSpan(new StyleSpan(Typeface.BOLD), 0, stdOut.length(), 0);

                                BufferedReader inputReader = new BufferedReader(new InputStreamReader(siac.getInputStream()));
                                String line;
                                while ((line = inputReader.readLine()) != null) {
                                    String toBeAppended = line.replace(siacFile.getAbsolutePath(), "siac");
                                    stdOut.append(toBeAppended + "\n");
                                }
                                inputReader.close();

                                stdOut.setSpan(new ForegroundColorSpan(MainActivity.defaultTextColor), enteredCommand.length() + 2, stdOut.length(), 0);
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        output.append(stdOut);
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        output = (TextView)v.findViewById(R.id.output);
        output.setMovementMethod(new ScrollingMovementMethod());
        Siad.getInstance(getActivity()).setTerminalFragment(this);
        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null)
            return;
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null)
            return;
        actionBar.setTitle("Terminal");
    }

    public void appendToOutput(String text) {
        output.append(text);
    }

    public void onResume() {
        super.onResume();
        output.append(Siad.getInstance(getActivity()).getBufferedStdout());
    }
}
