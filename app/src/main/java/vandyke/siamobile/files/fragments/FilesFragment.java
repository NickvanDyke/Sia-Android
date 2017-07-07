package vandyke.siamobile.files.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import vandyke.siamobile.R;

public class FilesFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files, container, false);
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
        actionBar.setTitle("Files");
    }
}
