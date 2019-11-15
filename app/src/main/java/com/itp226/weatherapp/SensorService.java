package com.itp226.weatherapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Timer;
import java.util.TimerTask;

// This service runs periodically.
// When this service is killed, it broadcasts a request to the
// RestartReceiver. The receiver will check the sensor setting
// and determine whether to restart the service.
// This service periodically goes online to download some
// weather data from openweathermap.org
// To be able to access the API, user must register to create an account.
// An API key will be sent to the user's email account.
// It may take a few hours for the API key to be activated.
// Put the API key in the appID field to run
public class SensorService extends Service {
    private static final String myId = SensorService.class.getSimpleName();

    private long TimerDelay = 1000; // Amount of delay before timer starts
    private long TimerPeriod = 5000; // Time interval between successive task executions

    // TODO: Obtain API key and paste here
    private String appID = "PUT_YOUR_API_KEY_HERE";
    // TODO: Allow the user to change city through a UI
    private String cityID = "4771426"; // ID for City of Manassas

    private String weatherAPI = "http://api.openweathermap.org/data/2.5/weather?id="+cityID+"&APPID="+appID;

    // TODO: Allow the user to change temperature threshold through a UI
    private double threshold = 30;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        // prevent the system from restarting this service if it is killed.
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(myId, "onDestroy");
        // Stop the timer
        if (timer!=null) {
            timer.cancel();
            timer=null;
        }
        // send new broadcast to restart the service
        // when this service is destroyed.
        Intent broadcastIntent = new Intent("com.itp226.weatherapp.RestartReceiverIntent");
        sendBroadcast(broadcastIntent);
    }

    private Timer timer;
    private TimerTask timerTask;


    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        timerTask = new TimerTask() {
            public void run() {
                // The current simple task is to go online to download weather data periodically.
                // When the temperature is above certain threshold value, alert the user
                String data = getAPI();
                if (data.length()>0) {
                    Log.i(myId, "Data = " + data);
                    if (checkTemperature(data)) {
                        Intent broadcastIntent = new Intent("com.itp226.weatherapp.AlertReceiverIntent");
                        sendBroadcast(broadcastIntent);
                    }
                }
            }
        };

        //schedule the timer to start after a delay and to execute every X second
        timer.schedule(timerTask, TimerDelay, TimerPeriod); //
    }


    // This method uses the internet to download weather data
    // The data arrives as a string of JSON record
    String getAPI() {
        String data = "";
        try {
            // Access the API to get the data
            URL url = new URL(weatherAPI);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            // Assume that the data is within 1000 bytes.
            ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
            int readsize = readableByteChannel.read(byteBuffer);
            // Convert the data to a string
            byteBuffer.rewind();
            byte[] bytes = new byte[readsize];
            byteBuffer.get(bytes);
            data = new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // This method extracts out the temperature data from the JSON record
    // and compare the temperature with a threshold value
    boolean checkTemperature(String data) {
        try {
            // a few interesting JSON fields of a record
            // For details on the JSON format, see https://openweathermap.org/current
            final String JSON_main = "main";
            final String JSON_temp = "temp";
            // Extract out the temperature from the record
            JSONObject jsonObject = new JSONObject(data);
            double temperature = jsonObject.getJSONObject(JSON_main).getDouble(JSON_temp);
            // Do a quick check of the temperature. Return true
            // if it is above threshold
            if (temperature>threshold)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
