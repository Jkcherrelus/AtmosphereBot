package sol_ar.atmospherebot.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import sol_ar.atmospherebot.R;

/**
 * Created by Anansi on 10/29/2016.
 */
public class     RobotFragment extends Fragment
{
    private int MAX_SIZE = 50;
    private String [] temperature = new String[MAX_SIZE];
    private String [] pressure = new String[MAX_SIZE];
    private String [] altitude = new String[MAX_SIZE];
    private String [] seaLevel = new String[MAX_SIZE];
    private int currentLocation;
    protected String url;
    protected String databaseUrl;
    private ProgressBar progressBar;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_robot, container, false);

        progressBar = (ProgressBar)rootView.findViewById(R.id.spinner);
        //start progress bar
        progressBar.setVisibility(View.VISIBLE);
        //connect to the robot
        String ssid = "Hello_IoT";
        String password = "12345678";

        //configure the robot network
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + ssid +"\"";
        wifiConfig.preSharedKey = "\"" + password + "\"";


        WifiManager wifiManager = (WifiManager) rootView.getContext().
                getSystemService(Context.WIFI_SERVICE);

        //make sure wifi is on
        wifiManager.setWifiEnabled(true);

        //adds the wifi config to device
        int netId = wifiManager.addNetwork(wifiConfig);

        //disable from all wifi networks
        wifiManager.disconnect();
        //enables only this network
        wifiManager.enableNetwork(netId,true);
        //connect to enabled network
        wifiManager.reconnect();


        //location information

        url = "http://192.168.4.1";
        databaseUrl = "http://104.236.203.136/add.php";
        final WebView webView = (WebView) rootView.findViewById(R.id.webView);

        currentLocation = 0;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }
        });

        progressBar.setVisibility(View.GONE);
        //write data to text file
        final String databaseFile = "";
        String fileName = "TempData";


        if (currentLocation <= MAX_SIZE) {
            //get the data from the html and add to array
            if (Build.VERSION.SDK_INT <= 19) {
                webView.evaluateJavascript("(function() " +
                                "var s = document.getElementById(\"temp\"); return s;})()",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                temperature[currentLocation] = s;
                            }
                        });


                webView.evaluateJavascript("(function() " +
                                "var s = document.getElementById(\"pressure\"); return s;})()",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                pressure[currentLocation] = s;
                            }
                        });

                webView.evaluateJavascript("(function() " +
                                "var s = document.getElementById(\"sealevel\"); return s;})()",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                seaLevel[currentLocation] = s;
                            }
                        });

                webView.evaluateJavascript("(function() " +
                                "var s = document.getElementById(\"altitude\"); return s;})()",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                altitude[currentLocation] = s;
                            }
                        });
                currentLocation++;
                //show the temperature is being read
                Toast.makeText(rootView.getContext(),
                        "The temp is "+ temperature[currentLocation],
                        Toast.LENGTH_LONG).show();
                databaseFile.concat("\n"+temperature[currentLocation]
                +"|"+pressure[currentLocation]
                +"|"+seaLevel[currentLocation]
                +"|"+altitude[currentLocation]);

            }


        }
        if (currentLocation == MAX_SIZE)
        {
            //switch wifi connnection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Toast.makeText(this.getContext(),
                        "Data limit reached please connect to database",
                        Toast.LENGTH_LONG).show();
            }
            //switch wifi networks
            wifiManager.disconnect();
            Toast.makeText(rootView.getContext(), "Connecting to open network...",Toast.LENGTH_LONG).show();
            wifiManager.disableNetwork(netId);
            wifiManager.reconnect();
            wifiManager.enableNetwork(netId,false);

            ConnectionDatabase.execute(new Runnable() {
                @Override
                public void run() {
                    new ConnectionDatabase().execute();
                }
            });
            currentLocation = 0;
        }


        return rootView;
    }


    public class ConnectionDatabase extends AsyncTask<String,String,String>
    {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... strings)
        {
            try {
                String urlParam = "temp="+temperature[currentLocation]
                        +"&pressure="+ pressure[currentLocation]
                        +"&seaLevel="+ seaLevel[currentLocation]
                        +"&altitude="+ altitude[currentLocation];
                byte[] postData = urlParam.getBytes(StandardCharsets.UTF_8);
                int postLength = postData.length;

                //connect to the database server
                HttpURLConnection connection;
                URL MyUrl = new URL(databaseUrl);
                connection = (HttpURLConnection) MyUrl.openConnection();

                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("Content-Length", Integer.toString(postLength));
                connection.setUseCaches(false);
                try(DataOutputStream wr = new DataOutputStream(connection.getOutputStream()))
                {
                    wr.write(postData);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        private void sendData()
        {
            try {
                URL url = new URL(databaseUrl);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                //puts all of the data we saved into name value pair
                ContentValues values = new ContentValues();
                for(int i = 0; i<currentLocation;i++) {
                    values.put("temperature", temperature[i]);
                    values.put("pressure",pressure[i]);
                    values.put("seaLevel",seaLevel[i]);
                    values.put("altitude",altitude[i]);
                    //send data to server
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));

                    writer.write(dataToSend(values));
                    writer.flush();
                    writer.close();
                    os.close();
                }


                conn.connect();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //returns the name value pair as a readable parameter
        private String dataToSend(ContentValues params) throws UnsupportedEncodingException
        {
            StringBuilder result = new StringBuilder();
            boolean first = true;

            for (Map.Entry<String,Object> entry : params.valueSet())
            {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
            }

            return result.toString();
        }

    }

}
