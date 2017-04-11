package sol_ar.atmospherebot.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sol_ar.atmospherebot.R;

/**
 * Created by Anansi on 10/29/2016.
 */
public class MainFragment extends Fragment {

    public MainFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main,container,false);

        return rootView;
    }
}
