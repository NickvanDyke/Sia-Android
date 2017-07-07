/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.help.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;

public class HelpFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help, null);

        final Button remoteButton = (Button)v.findViewById(R.id.remoteSetup);
        remoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity)getActivity()).loadDrawerFragment(FragmentSetupRemote.class);
            }
        });

        final Button localButton = (Button)v.findViewById(R.id.localSetup);
        localButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity)getActivity()).loadDrawerFragment(FragmentSetupLocal.class);
            }
        });

        final Button coldButton = (Button)v.findViewById(R.id.coldStorageSetup);
        coldButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity)getActivity()).loadDrawerFragment(FragmentSetupCold.class);
            }
        });

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
        actionBar.setTitle("Help");
    }
}
