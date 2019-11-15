package com.itp226.weatherapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

// This BroadcastReceiver will restart the service when
// the previous service is killed and the sensor setting
// is ON.
public class RestartReceiver extends BroadcastReceiver {
    private static String myId = RestartReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(myId, "Restart broadcast received");

        // Check sensor setting from SharedPreference.
        // If it is set to on, then restart even when activity does not exist.
        boolean isOn = context.getSharedPreferences(MainActivity.PreferenceFile, MODE_PRIVATE).getBoolean(MainActivity.PreferenceKey, false);
        if (isOn) {
            Log.i(myId, "Restarting Service");
            context.startService(new Intent(context, SensorService.class));
        }
        else {
            Log.i(myId, "Stop Service");
        }
    }
}