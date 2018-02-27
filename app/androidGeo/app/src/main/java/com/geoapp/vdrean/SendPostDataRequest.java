package com.geoapp.vdrean;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by vdrean on 11/10/2017.
 */

public class SendPostDataRequest extends AsyncTask<JSONObject, Void, JSONObject>
{
    private String strUrl = "API REST URL";
    private String logSendPostDataRequest = "SendPostDataRequest";
    private HttpURLConnection httpPost = null;
    private URL url;
    private OutputStreamWriter wr;
    private InputStream in;
    private StringBuffer sb;

    public AsyncResponse delegate = null;

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(JSONObject... params) {

        if (params != null)
        {
            try
            {
                // CONNEXION TO REST & CONFIG JSON
                url = new URL(strUrl);
                httpPost = (HttpURLConnection) url.openConnection();
                httpPost.setConnectTimeout(5000);

                // REQUEST METHOD && HEADER
                httpPost.setRequestMethod("POST");
                httpPost.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpPost.setRequestProperty("Accept", "application/json");

                // IO PERMISSIONS & CONNECTION
                httpPost.setDoOutput(true);
                httpPost.setDoInput(true);
                httpPost.connect();

                wr = new OutputStreamWriter(httpPost.getOutputStream());

                // params[0] is JSONObject because of postRequest.execute(jsonPost)
                // in ProcessData.class
                wr.write(params[0].toString());
                Log.d(logSendPostDataRequest, "Sending " + params[0]);
                wr.flush();

                // RESPONSE FROM SERVER ONLY FOR DEBUGGING
                // NO NEED TO BOTHER USER WITH LOST PACKAGES BECAUSE WILL BE SENT LATER
                in = httpPost.getInputStream();
                sb = new StringBuffer();
                try
                {
                    int chr;
                    while ((chr = in.read()) != -1)
                    {
                        sb.append((char) chr);
                    }
                    String reply = sb.toString();
                    Log.d(logSendPostDataRequest, "response from post : " + reply);
                }
                finally
                {
                    in.close();
                }
                wr.close();

                // return null to postExecute because params[0] successfully sent to Web Service
                return null;
            }
            catch (IOException e)
            {
                Log.d(logSendPostDataRequest, e + " via : " + strUrl + " fail");

                // return JSONObject sent to postExecute because couldn't connect to Web Service
                return params[0];
            }
            finally
            {
                if (httpPost != null)
                {
                    httpPost.disconnect();
                }
            }
        }
        else
        {
            Log.d(logSendPostDataRequest, "json null !" + params);
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonPost)
    // JSONObject from doInBackground so JSONObject in onPostExecute param
    {
        super.onPostExecute(jsonPost);
        // recover JSONObject from doInBackground because couldn't connect to API REST
        delegate.processFinish(jsonPost);
    }
}