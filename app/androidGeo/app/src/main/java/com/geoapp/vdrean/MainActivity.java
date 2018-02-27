package com.geoapp.vdrean;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity
{
    private Context context = this;
    private String logMain = "logMain";

    private TextView tvCoordinates;
    private TextView tvLogged;
    private Button btnLogout;
    private String prefIdUser;
    private String prefUserName;
    private String prefUserLastName;

    private ProcessData processData;
    private VerifyNet verifyNetwork = null;

    protected void init()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        prefIdUser = sharedPref.getString("prefIdUser", null);
        // get prefIdUser or put value to null
        prefUserName = sharedPref.getString("prefNameUser", null);
        // get prefIdUser or put value to null
        prefUserLastName = sharedPref.getString("prefIdLastNameUser", null);
        // get prefIdUser or put value to null
        tvLogged = (TextView) findViewById(com.geoapp.vdrean.R.id.tvLogged);

        tvLogged.setText(prefUserName + " " + prefUserLastName);

        btnLogout = (Button) findViewById(com.geoapp.vdrean.R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SharedPreferences sharedPref =
                        PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPref.edit();

                editor.remove("prefIdUser");
                editor.remove("prefNameUser");
                editor.remove("prefIdLastNameUser");
                editor.commit();

                processData.stop();

                Intent loginIntent = new Intent(context, LogInActivity.class);
                context.startActivity(loginIntent);
                ((Activity) context).finish();

                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show();

            }
        });
        processData = new ProcessData(this, Integer.parseInt(prefIdUser));
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(com.geoapp.vdrean.R.layout.activity_main);

        final ToggleButton togglebutton = (ToggleButton) findViewById(com.geoapp.vdrean.R.id.togglebutton);
        tvCoordinates = (TextView) findViewById(com.geoapp.vdrean.R.id.TestCoordonne);

        // RequestPermissions for all location mode
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);

        if (PermissionChecker.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(context, "Please activate location permissions",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            Toast.makeText(context, "Location permissions granted", Toast.LENGTH_SHORT).show();
        }

        // Start & stop processing data
        togglebutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (prefIdUser != null)
                {
                    if (isChecked)
                    {
                        // Check if internet connexion here & not in ProcessData because don't
                        // need to bother user when lost connexion for a few seconds
                        if (verifyNetwork.isInternetOn(context))
                        {
                            tvCoordinates.setText("Awaiting location change...");
                            processData.start();
                        }
                        else
                        {
                            // No need to warn user with toast because isInternetOn already does it
                            togglebutton.setChecked(false);
                        }
                    }
                    else
                    {
                        Toast.makeText(context, "Sending last coordinates before stop",
                                Toast.LENGTH_LONG).show();
                        processData.stop();
                    }
                }
            }
        });
        init();
    }

    // Warning user to activate location permissions
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults)
    {
        switch (requestCode)
        {
            case 10:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(logMain, "Auth location granted" );
                }
                else
                {
                    Toast.makeText(context, "Please activate location permissions",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}

