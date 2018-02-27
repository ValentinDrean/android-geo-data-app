package com.geoapp.vdrean;

import org.json.JSONObject;

/**
 * Created by vdrean on 18/10/2017.
 */

// Giving back JSONObject if doInBackground failed to send it to Web Service
public interface AsyncResponse
{
    void processFinish(JSONObject jsonPost);
}
