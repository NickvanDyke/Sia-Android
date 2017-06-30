package vandyke.siamobile.fragments;

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
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Help");

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
}
