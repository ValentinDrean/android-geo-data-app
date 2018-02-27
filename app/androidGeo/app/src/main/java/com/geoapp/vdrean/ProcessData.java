package com.geoapp.vdrean;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vdrean on 16/10/2017.
 */

public class ProcessData implements LocationListener, AsyncResponse {


    private final String logProcessData = "ProcessData";
    private Context context;

    // Location
    private LocationManager locationManager;
    private Handler handler = new Handler();
    private final Integer interval = 5000;
    private Location location;
    private boolean isGPS = false;
    private boolean isNetwork = false;
    private boolean loopFlag = false;
    private String whichProvider =  null;

    // User & data
    private int userId;
    private Double lat;
    private Double lng;
    private Integer dataType = 4;
    private Integer hardId = 3;

    // AsyncTask object
    private SendPostDataRequest postDataRequest;

    // Construct with context and userId from MainActivity
    public ProcessData(Context mainContext, int mainUserId)
    {
        context = mainContext;
        userId = mainUserId;

        // Location variable starters
        locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d(logProcessData,
                "Debut ProcessData() call : isGPS " + isGPS + " isNetwork " + isNetwork);
    }

    // MANDATORY LOCATION METHODS
    @Override
    public void onLocationChanged(Location location)
    {
        if (isGPS)
        {
            Log.d(logProcessData,
                    "onLocationChanged() PROVIDER ? " + LocationManager.GPS_PROVIDER);
            lat = location.getLatitude();
            lng = location.getLongitude();

        }
        else // means (isNetwork), need this later if High Accuracy Location Mode
        {
            Log.d(logProcessData,
                    "onLocationChanged() PROVIDER ? " + LocationManager.NETWORK_PROVIDER);
            lat = location.getLatitude();
            lng = location.getLongitude();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        launchLocationRequestUpdates();
    }

    // If no provied, launch settings to activate location
    @Override
    public void onProviderDisabled(String provider)
    {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    // When toggle button from Main ON
    public void start()
    {
        loopFlag = true;
        launchLocationRequestUpdates();

    }

    // When toggle button from Main OFF
    public void stop()
    {
        locationManager.removeUpdates(this);
        loopFlag = false;
    }

    // Testing if FINE or COARSE location
    // Launching LocationUpdates & LocationTimer
    private void launchLocationRequestUpdates()
    {
        try
        {
            if (isGPS)
            {
                // from GPS
                whichProvider = "GPS !";
                Log.d(logProcessData, "GPS_PROVIDER on");
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        interval,
                        0, this);

                if (locationManager != null)
                {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null)
                    {
                        handler.post(getLocationTimer);
                    }

                }
            }
            else // means if(isNetwork)
            {
                // from Network Provider
                whichProvider = "NETWORK !";
                Log.d(logProcessData, "NETWORK_PROVIDER on");
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        interval,
                        0, this);

                if (locationManager != null)
                {
                    location =
                            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null)
                    {
                        handler.post(getLocationTimer);
                    }
                }
            }
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
    }

    // if button ON calls dataCreateJSONPost() every 5000
    private final Runnable getLocationTimer = new Runnable()
    {
        public void run()
        {
            try
            {
                dataCreateJSONPost();
                if(loopFlag)
                    handler.postDelayed(this, interval);
            }
            catch (Exception e) {
                Log.d(logProcessData,
                        "Processing thread id" + Thread.currentThread().getId());
                Log.d(logProcessData,
                        "getLocationTimer calls dataCreateJSONPost()" + e);
            }
        }
    };

    // Adapting java.util.Date to mySQL DateFormat
    public String getCurrentTime()
    {
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(dt);
        return currentTime;
    }

    // JSON CREATION
    private void dataCreateJSONPost()
    {
        // Create new object each dataCreateJSONPost call because if not sent,
        // we will need it to send it later to api rest
        JSONObject jsonPost = new JSONObject();
        if (lat != null && lng !=null)
        {
            try
            {
                jsonPost.put("user_data_id", userId);
                jsonPost.put("data_time", getCurrentTime()); //2001-08-22 00:00:00
                jsonPost.put("data_lat", lat);
                jsonPost.put("data_long", lng);
                jsonPost.put("data_type_id", dataType);
                jsonPost.put("data_hard_id", hardId);

                sendJSONToWeb(jsonPost);
                updateTextUI();
            }
            catch (JSONException e)
            {
                Log.d(logProcessData, e + " in the JSONException catch");
            }
        }
        else
        {
            Log.d(logProcessData,
                    "no sendJSONToWeb because" + " lat " + lat + " & long " + lng);
        }
    }

    // JSON SENT TO ORBIT
    private void sendJSONToWeb(JSONObject jsonPost)
    {
        Log.d(logProcessData, "SendPostDataRequest go !");
        // ASyncTask object every call of sendJSONToWeb
        postDataRequest = new SendPostDataRequest();
        postDataRequest.delegate = this;
        postDataRequest.execute(jsonPost); // sending JSONObject to AsyncTask
    }

    // Sending to UI what data is currently sending and with its properties
    public void updateTextUI()
    {
        TextView textView = (TextView) ((Activity)context).findViewById(com.geoapp.vdrean.R.id.TestCoordonne);
        textView.setText("Currently sending to user : " + userId + "\n "
                + "lat : " + lat + " long : " + lng + "\n"
                + "GPS : " + isGPS + " & NETWORK : " + isNetwork + "\n"
                + "Using provider : " + whichProvider + "\n"
                + "dataType : " + dataType + " & hardId : " + hardId + "\n"
                + "Current Time : \n" + getCurrentTime());
    }

    // Recover of JSONObject not sent from doInbackground, then from postExecute, then
    // from interface AsyncResponse, then from this method :
    // then trying to sendJSONToWeb(jsonPost) again.
    @Override
    public void processFinish(JSONObject jsonPost)
    {
        if(jsonPost!=null)
        {
            sendJSONToWeb(jsonPost);
        }
    }
}