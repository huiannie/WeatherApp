package com.itp226.weatherapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

// This activity allows the user to configure the
// setting of the sensor service.
// The user may turn the service on/off.
// The sensor setting is stored as a SharedPreference
// so that it is retrievable by BroadcastReceivers
// even when the activity itself no longer exists.
public class MainActivity extends AppCompatActivity {
    private static final String myId = MainActivity.class.getSimpleName();

    Intent mServiceIntent;

    // Set a sensor setting to decide whether to run sensor or not
    public static final String PreferenceFile = "PeriodicUpdates";
    public static final String PreferenceKey = "Sensor";
    ToggleButton toggleButton;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServiceIntent = new Intent(MainActivity.this, SensorService.class);

        // TextView to show status
        textView = findViewById(R.id.status);
        textView.setText("No service");

        // ToggleButton to turn on or off the service
        toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isOn) {
                getSharedPreferences(PreferenceFile, MODE_PRIVATE).edit().putBoolean(PreferenceKey, isOn).commit();
                if (isOn) {
                    if (!isServiceRunning()) {
                        startService(mServiceIntent);
                    }
                    // else: already running
                    textView.setText("Service is running. ");
                }
                else {
                    stopService(mServiceIntent);
                    textView.setText("Service is stopped.");
                }
            }
        });
    }


    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (SensorService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean state = getSharedPreferences(PreferenceFile, MODE_PRIVATE).getBoolean(PreferenceKey, false);
        toggleButton.setChecked(state);
    }

    @Override
    protected void onDestroy() {
        Log.i(myId, "Activity destroyed.");
        super.onDestroy();
    }
}
