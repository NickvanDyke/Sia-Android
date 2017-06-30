package vandyke.siamobile.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import vandyke.siamobile.MainActivity;
import vandyke.siamobile.R;

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
                    ((MainActivity)getActivity()).loadDrawerFragment(AboutFragment.class);
                }
            });
            return v;
        }
}
