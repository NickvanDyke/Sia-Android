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
import vandyke.siamobile.misc.LinksFragment;

public class WelcomeFragment extends Fragment {

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_welcome, null);
            Button setupButton = (Button)v.findViewById(R.id.setupButton);
            Button infoButton = (Button)v.findViewById(R.id.infoButton);
            setupButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ((MainActivity)getActivity()).loadDrawerFragment(HelpFragment.class);
                }
            });
            infoButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ((MainActivity)getActivity()).loadDrawerFragment(LinksFragment.class);
                }
            });
            return v;
        }
}
