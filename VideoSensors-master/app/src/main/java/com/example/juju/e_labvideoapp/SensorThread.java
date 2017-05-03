package com.example.juju.e_labvideoapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by augustinj on 19/04/2017.
 */

public class SensorThread implements Runnable {

    public SensorManager sensorManager = null;
    public Sensor sensor = null;
    public int sensorType = 0;

    public long timeS;

    public SensorThread(SensorManager sensorManager, int sensorType){
        this.sensorManager = sensorManager;
        this.sensorType = sensorType;
        sensor = sensorManager.getDefaultSensor(sensorType);
    }

    public SensorEventListener getGyroListener() {
        return sensorListener;
    }

    final SensorEventListener sensorListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Que faire en cas de changement de précision ?
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            // Que faire en cas d'évènements sur le capteur ?
            timeS = System.currentTimeMillis();
            float valX = sensorEvent.values[0];
            float valY = sensorEvent.values[1];
            float valZ = sensorEvent.values[2];

            Log.i("Thread sensor nb "+sensorType+" :","Time : "+ timeS +" : X --> "+valX+", Y --> "+valY+", Z --> "+valZ+" !");
        }
    };

    @Override
    public void run() {
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public long getTimeS() {
        return timeS;
    }
}
