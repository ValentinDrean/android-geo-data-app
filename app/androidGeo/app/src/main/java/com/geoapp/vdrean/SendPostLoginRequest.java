package com.geoapp.vdrean;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by vdrean on 28/11/2017.
 */

public class SendPostLoginRequest extends AsyncTask<JSONObject, Void, JSONObject>
{
    private String strUrl = "API REST LOGIN URL";
    // will need token for production use
    private String logSendPostLoginRequest = "SendPostLoginRequest";
    private JSONObject jsonResponse;
    private Context context;
    private String badUser;
    private String userIdResponse;
    private String userNameResponse;
    private String userLastNameResponse;

    private HttpURLConnection httpPost = null;
    private URL url;
    private OutputStreamWriter wr;
    private InputStream in;
    private StringBuffer sb;

    public SendPostLoginRequest(Context loginContext)
    {
        context = loginContext;
    }

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
                // CONNEXION A REST & CONFIG JSON
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

                // params[0] is JSONObject because of postRequest.execute(jsonPost) in ProcessLoginData.class
                wr.write(params[0].toString());
                Log.d(logSendPostLoginRequest, "Sending " + params[0]);
                wr.flush();

                // RESPONSE FROM SERVER AFTER POST
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
                    try
                    {
                        jsonResponse = new JSONObject(reply);
                        Log.d(logSendPostLoginRequest, "response from post in json object format : " + jsonResponse);
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                } finally {
                    in.close();
                }
                wr.close();

                // return jsonResponse to onPostExecute to get user credentials on MaineActivity
                return jsonResponse;
            }
            catch (IOException e)
            {
                Log.d(logSendPostLoginRequest, e + " via : " + strUrl + " fail");

                // Return jsonResponse/params[0] to get error message from Web Service
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
            Log.d(logSendPostLoginRequest, "json null !" + params);
        }
        return jsonResponse;
    }

    @Override
    protected void onPostExecute(JSONObject jsonResponse) {
        super.onPostExecute(jsonResponse);

        try {
            // Success login !
            if (jsonResponse.getBoolean("success"))
            {
                userIdResponse = (String) jsonResponse.get("user_id");
                userNameResponse = (String) jsonResponse.get("user_name");
                userLastNameResponse = (String) jsonResponse.get("user_lastname");

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("prefIdUser", userIdResponse);
                editor.putString("prefNameUser", userNameResponse);
                editor.putString("prefIdLastNameUser", userLastNameResponse);
                editor.apply();

                Log.d(logSendPostLoginRequest, "userIdResponse saved to pref");

                Toast.makeText(context, "Welcome to you " + userNameResponse + " " + userLastNameResponse + " !", Toast.LENGTH_LONG).show();

                Intent mainIntent = new Intent(context, MainActivity.class);

                context.startActivity(mainIntent);
                ((Activity) context).finish();

            }
            else // Bad login, sending error from web service to user !
            {
                badUser = (String) jsonResponse.get("message");
                Toast.makeText(context, badUser, Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
