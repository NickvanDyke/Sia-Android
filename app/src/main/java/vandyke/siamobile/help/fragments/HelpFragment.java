/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.help.fragments;

import android.app.Fragment;
import android.os.Bundle;
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
                ((MainActivity)getActivity()).displayFragment(FragmentSetupRemote.class, "Setup");
            }
        });

        final Button localButton = (Button)v.findViewById(R.id.localSetup);
        localButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity)getActivity()).displayFragment(FragmentSetupLocal.class, "Setup");
            }
        });

        final Button coldButton = (Button)v.findViewById(R.id.coldStorageSetup);
        coldButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity)getActivity()).displayFragment(FragmentSetupCold.class, "Setup");
            }
        });

        return v;
    }
}
