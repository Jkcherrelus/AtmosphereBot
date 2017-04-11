package sol_ar.atmospherebot.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import java.net.URL;

import sol_ar.atmospherebot.R;

/**
 * Created by Anansi on 11/26/2016.
 */
public class DatabaseFragment extends Fragment
{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_database,container,false);



        //url to the database
        final String url = "http://104.236.203.136";

        //shows the items in the database
        final WebView webView =(WebView) rootView.findViewById(R.id.databaseWebView);
        //starts web page in the app
        webView.loadUrl(url);

        Button button =(Button) rootView.findViewById(R.id.pageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                webView.loadUrl(url);
            }
        });
        return rootView;
    }

    private void getOpenNetwork()
    {

    }
}
