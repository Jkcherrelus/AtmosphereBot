package sol_ar.atmospherebot;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created by Anansi on 11/6/2016.
 */
public class Communication extends AsyncTask<Void,Void,Void>
{
    private String IP = "192.241.138.244";
    private URL url;
    private HttpURLConnection connect;
    Context context;

    //get ip of server
    public Communication(Context context)
    {
        this.context = context;
    }

    //create a connection
    public void setConnection() throws IOException {
        connect = (HttpURLConnection) (url).openConnection();
        connect.setRequestMethod("POST");
        connect.setDoOutput(true);
        connect.setDoInput(true);
        connect.connect();
    }

    //write to arduino
    public void writeOutput(String output) throws IOException {
        connect.getOutputStream().write((output).getBytes());
    }
    //get data from arduion
    public String receiveData(String data) throws IOException {
        InputStream is = connect.getInputStream();
        byte[] b = new byte[1024];
        StringBuffer buffer = new StringBuffer();
        while(is.read(b) != -1)
        {
            buffer.append(new String(b)).toString();
        }
        data = buffer.toString();
        connect.disconnect();
        return data;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}



