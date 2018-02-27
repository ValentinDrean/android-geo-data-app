package com.geoapp.vdrean;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vdrean on 07/12/2017.
 */

class ProcessLoginData
{
    private String logSendPostLoginRequest = "SendPostLoginRequest";
    private Context context;
    private SendPostLoginRequest postLoginRequest;

    public ProcessLoginData(Context loginContext)
    {
        context = loginContext;
        postLoginRequest = new SendPostLoginRequest(context);
    }

    // Sending email & pw to web service to verify if account exists & can log in
    public void loginCreateJSONPost(String emailUser, String pwUser)
    {
        JSONObject jsonPost = new JSONObject();
        if (emailUser != null && pwUser != null)
        {
            try
            {
                jsonPost.put("emailinput", emailUser);
                jsonPost.put("pwinput", pwUser);
                sendJSONToWeb(jsonPost);
            } catch (JSONException e)
            {
                Log.d(logSendPostLoginRequest, e + " in the JSONException catch");
            }
        }
        else
        {
            Log.d(logSendPostLoginRequest, "no sendJSONToWeb because" + " emailUser " + emailUser + " & pwUser " + pwUser);
        }
    }

    // JSON SENT TO ORBIT
    private void sendJSONToWeb(JSONObject jsonPost)
    {
        Log.d(logSendPostLoginRequest, "SendPostLoginRequest go !");

        // ASyncTask call
        postLoginRequest.execute(jsonPost);
    }
}
