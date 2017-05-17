package com.example.juju.e_labvideoapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by augustinj on 19/04/2017.
 */

public class SensorThread extends Thread {

    public SensorManager sensorManager = null;
    public Sensor sensor = null;
    public int sensorType = 0;
    // Printer to write in a CSV file
    public PrintWriter writer = null;
    public String timeStampFile;
    // Name of the sensor
    public String sensorName = null;
    // Used to know when to launch and stop the threads
    static boolean running = true;
    // Time in ms at which the video started
//    public long timeS;

    public SensorThread(SensorManager sensorManager, int sensorType, String timeStampFile){
        this.sensorManager = sensorManager;
        this.sensorType = sensorType;
        this.timeStampFile = timeStampFile;
//        this.timeS = timeS;
        sensor = sensorManager.getDefaultSensor(sensorType);
        sensorName = sensorManager.getDefaultSensor(sensorType).getName();
        // Creation of the file in which the values of the sensor will be stored
        // We will then have one file per sensor
        String filePath = Environment.getExternalStorageDirectory().getPath()+"/elab/" + timeStampFile + "/" + sensorName.replace(" ", "_")+timeStampFile +".csv";
        try {
            writer = new PrintWriter(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Log.i("SensorThread","The writer !");
        writer.println(sensorName + "," + "Time Stamp:"+timeStampFile);
        writer.println("Time (nanosec)" + "," + "Value X" + "," + "Value Y" + "," + "Value Z");
    }

    public SensorEventListener getGyroListener() {
        return sensorListener;
    }

    final SensorEventListener sensorListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Que faire en cas de changement de précision ?
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            if(running) {
                Log.i("SensorThread","Running is true");

                // Que faire en cas d'évènements sur le capteur
                float valX = sensorEvent.values[0];
                float valY = sensorEvent.values[1];
                float valZ = sensorEvent.values[2];

                Long timeStamp = System.nanoTime();
                writer.println(timeStamp + "," + valX + "," + valY + "," + valZ);
                Log.i("SensorThread","value X :"+valX);
            } else {
                Log.i("SensorThread","running is false, we stop everything");

                sensorManager.unregisterListener(this);
                writer.close();
            }
        }
    };

    @Override
    public void run() {
        running = true;
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public static void stopTh(){
        Log.i("SensorThread","we change running to false");
        running = false;
    }

}
