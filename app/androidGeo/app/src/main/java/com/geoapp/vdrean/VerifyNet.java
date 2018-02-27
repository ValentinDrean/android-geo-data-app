package com.geoapp.vdrean;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by vdrean on 08/12/2017.
 */

class VerifyNet
{
    private static final String logVerifyNet = "VerifyNet";

    // Checking if internet connection ON
    public static boolean isInternetOn(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected())
        {
            // No need to bother user when everything's ok :)
            Log.d(logVerifyNet, "Internet is working");
            return true;
        }
        else
        {
            Toast.makeText(context, "No internet connection...", Toast.LENGTH_LONG).show();
            Log.d(logVerifyNet, "No internet connection...");
            return false;
        }
    }
}
