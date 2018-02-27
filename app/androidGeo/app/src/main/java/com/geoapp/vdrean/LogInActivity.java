package com.geoapp.vdrean;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by vdrean on 27/11/2017.
 */

//TODO: SORT CLASS MEMBERS

public class LogInActivity extends AppCompatActivity
{
    private String logLoginActivity = "logLoginActivity";

    private EditText editEmail;
    private EditText editPW;
    private Button btnLogin;
    private Context context = this;
    private SharedPreferences sharedPref;
    private String prefIdUser;
    private VerifyNet verifyNet = null;
    private ProcessLoginData processLoginData;

    protected void init()
    {
        // Starting Shared Preferences object to save user credentials
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        prefIdUser = sharedPref.getString("prefIdUser", null); // get prefIdUser or put value to null

        // Automatically send user to Monitor Activity if already logged in once
        // Or else let user login on LogInActivity
        if (prefIdUser != null)
        {
            Log.d(logLoginActivity, "idUser saved ? Yes : " + prefIdUser);

            Intent mainIntent = new Intent(context, MainActivity.class);
            context.startActivity(mainIntent);
            ((Activity) context).finish();
        }

        editEmail = (EditText) findViewById(com.geoapp.vdrean.R.id.editEmail);
        editPW = (EditText) findViewById(com.geoapp.vdrean.R.id.editPW);
        btnLogin = (Button) findViewById(com.geoapp.vdrean.R.id.btnLogin);

        // Give context to ProcessLoginData class
        processLoginData = new ProcessLoginData(this);

        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!editEmail.getText().toString().trim().isEmpty() && !editPW.getText().toString().trim().isEmpty())
                {
                    // Verify internet
                    if (verifyNet.isInternetOn(context))
                    {
                        processLoginData.loginCreateJSONPost(editEmail.getText().toString(), editPW.getText().toString());
                    }
                    // No need to Toast if !isInternetOn(context) because method already show toast
                }
                else
                {
                    Toast.makeText(context, "Empty values !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(com.geoapp.vdrean.R.layout.activity_login);
        init();
    }
}
