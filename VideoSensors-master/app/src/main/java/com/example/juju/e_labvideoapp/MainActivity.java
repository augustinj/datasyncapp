package com.example.juju.e_labvideoapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.camera.SCamera;
import com.samsung.android.sdk.camera.SCameraCharacteristics;
import com.samsung.android.sdk.camera.SCameraManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import static com.samsung.android.sdk.camera.impl.internal.ArrayUtils.contains;

public class MainActivity extends Activity {
    private Camera mCamera;
    private SCamera mScamera;
    private SCameraManager mSCameraManager;
    private CameraPreview mPreview;
    private MediaRecorder mediaRecorder;
    private ImageButton capture, vid;
    private Context myContext;
    private FrameLayout cameraPreview;
    private Chronometer chrono;
//    private TextView tv;
    private TextView txt;
    private long timeS;
    private long timeS1;

    private SensorThread gyroTh;
    private SensorThread accTh;

    int quality = 0;
    int rate = 100;
    String timeStampFile;
    int clickFlag = 0;
//    Timer timer;
    int VideoFrameRate = 24;

    private static final String TAG = "INFOS";

    LocationListener locationListener;
    LocationManager LM;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        head = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);

        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);

        capture = (ImageButton) findViewById(R.id.button_capture);
        capture.setOnClickListener(captureListener);

        chrono = (Chronometer) findViewById(R.id.chronometer);
        txt = (TextView) findViewById(R.id.txt1);
        txt.setTextColor(-16711936);

//        mScamera = new SCamera();
//        try {
//            mScamera.initialize(this);
//            //return;
//        } catch (SsdkUnsupportedException e){
//            int eType = e.getType();
//            if (eType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED)
//            {
//                // The device is not a Samsung device.
//                Log.i(TAG,"The device is not a samsung device");
//
//            } else if (eType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED)
//            {
//                // The device does not support Camera.
//                Log.i(TAG,"The device does not support Camera.");
//
//            } else if (eType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED)
//            {
//                // There is a SDK version mismatch.
//                Log.i(TAG,"There is a SDK version mismatch.");
//
//            }
//        }



//        vid = (ImageButton) findViewById(R.id.imageButton);
//        vid.setVisibility(View.GONE);

//        tv = (TextView) findViewById(R.id.textViewHeading);
//        String setTextText = "Heading: " + heading + " Speed: " + speed;
//        tv.setText(setTextText);


    }


    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public void onResume() {
        super.onResume();
        if (!checkCameraHardware(myContext)) {
            Toast toast = Toast.makeText(myContext, "Phone doesn't have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }

        mSCamera = new SCamera();
        try {
            mSCamera.initialize(this);
        } catch (SsdkUnsupportedException e) {
            showAlertDialog("Fail to initialize SCamera.", true);
            Log.i(TAG,"Fail to initialize SCamera.");
            return;
        }

//        mSCameraManager = mScamera.getSCameraManager();
//        int versionCode = mScamera.getVersionCode();
//        String versionName = mScamera.getVersionName();
//        boolean isOK = mScamera.isFeatureEnabled(SCamera.SCAMERA_IMAGE);
//        boolean isOK2 = mScamera.isFeatureEnabled(SCamera.SCAMERA_PROCESSOR);
//
//        Log.i(TAG,"Log : "+versionName+", log : "+versionCode+", log is Camera_Image : "+isOK+", Camera_Processor : "+isOK2+"--");

        if (!checkRequiredFeatures()) return;

        if (mCamera == null) {
            mCamera = Camera.open(findBackFacingCamera());
            mPreview.refreshCamera(mCamera);
        }



//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//        sensorManager.registerListener(this, head, SensorManager.SENSOR_DELAY_FASTEST);
//        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);



        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.

                latitude  = location.getLatitude();
                longitude = location.getLongitude();

                if(location.hasSpeed()) {
                    speed = location.getSpeed();
                }
                location.distanceBetween(latitude_original, longitude_original, latitude, longitude, dist);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Acquire a reference to the system Location Manager
        LM = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Register the listener with the Location Manager to receive location updates
        LM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // when on Pause, release camera in order to be used from other
        // applications
        releaseCamera();
//        sensorManager.unregisterListener(this);
//        gyroTh.stop();

    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


    boolean recording = false;
    OnClickListener captureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            if (recording) {
                timeS1 = 0;
                // stop recording and release camera
                mediaRecorder.stop(); // stop the recording
                releaseMediaRecorder(); // release the MediaRecorder object
                Toast.makeText(MainActivity.this, "Video captured!", Toast.LENGTH_LONG).show();
//                gyroTh.interrupt();
//                Sensor gyros = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//                sensorManager.unregisterListener(gyros.getGyroListener(), gyros);


                recording = false;
                //d.exportData();
                chrono.stop();
                chrono.setBase(SystemClock.elapsedRealtime());

                chrono.start();
                chrono.stop();
                txt.setTextColor(-16711936);
                chrono.setBackgroundColor(0);
                enddata();
/*
                if(clickFlag == 1){
                    clickFlag = 0;
                    capture.performClick();
                }
*/
            } else {

                timeS1 = System.nanoTime();
                timeStampFile = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File wallpaperDirectory = new File(Environment.getExternalStorageDirectory().getPath()+"/elab/");
                wallpaperDirectory.mkdirs();

                File wallpaperDirectory1 = new File(Environment.getExternalStorageDirectory().getPath()+"/elab/"+timeStampFile);
                wallpaperDirectory1.mkdirs();
                if (!prepareMediaRecorder()) {
                    Toast.makeText(MainActivity.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                    finish();
                }

                // Creation of the Sensor Threads
                gyroTh = new SensorThread(sensorManager, Sensor.TYPE_GYROSCOPE, timeStampFile);
                accTh = new SensorThread(sensorManager, Sensor.TYPE_ACCELEROMETER, timeStampFile);

                // Creation of the file where we write the System time of the launching of the video
                String filePath = Environment.getExternalStorageDirectory().getPath()+"/elab/" + timeStampFile + "/" + "Video_Start_Time_"+timeStampFile  +  ".csv";
                try {
                    writer = new PrintWriter(filePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // work on UiThread for better performance
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            timeS = System.nanoTime();
                            gyroTh.start();
                            accTh.start();
                            mediaRecorder.start();
                        } catch (final Exception ex) {
                            Log.i(TAG, "Problem launching the threads");
                        }
                    }
                });
                writer.println("Starting time of the Video");
                writer.println("! "+timeS+" ms");
                Toast.makeText(MainActivity.this, "Recording...", Toast.LENGTH_LONG).show();

                Camera.Parameters params = mCamera.getParameters();
                params.setPreviewFpsRange( 30000, 30000 ); // 30 fps
                if ( params.isAutoExposureLockSupported() )
                    params.setAutoExposureLock( true );

                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                // For 120 fps
//                params.set("fast-fps-mode",2);
//                params.setPreviewFpsRange(120000,120000);

                mCamera.setParameters(params);
                //d.beginData();
                storeData();
                chrono.setBase(SystemClock.elapsedRealtime());
                chrono.start();
                chrono.setBackgroundColor(-65536);
                txt.setTextColor(-65536);
                recording = true;
//                List<int[]> mList = mCamera.getParameters().getSupportedPreviewFpsRange();
//                for(int i =0; i <mList.size(); i++ ) {
//                    int[] ints = mList.get(i);
//                    for(int j = 0; j<ints.length; j++) {
//                        Log.i("AUGI TEST", "List of camera fps supported : " + ints[j] + " !");
//                    }
//                }

            }
        }
    };

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    private boolean prepareMediaRecorder() {

        mediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        if(quality == 0)
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
        else if(quality == 1)
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        else if(quality == 2)
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory().getPath()+"/elab/" + timeStampFile + "/" + timeStampFile  + ".mp4");
        mediaRecorder.setVideoFrameRate(VideoFrameRate);
        //mediaRecorder.setMaxDuration(5000);

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /* --------------------- Data Section ----------------------------*/

    Location location;
    LocationManager lm;
    double latitude = 0;
    double longitude = 0;

    double latitude_original = 0;
    double longitude_original = 0;
    //float distance = 0;
    float speed = 0;
    float dist[] = {0,0,0};
    PrintWriter writer = null;
    long timechecker = 5000;

    class SayHello extends TimerTask {
        public void run() {
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener );
            //longitude = location.getLongitude();
            //latitude = location.getLatitude();
            //if(location.hasSpeed()) {
              //  speed = location.getSpeed();
            //}
            //dist[0] = (float) 0.0;
            /*
            long elapsedMillis = SystemClock.elapsedRealtime() - chrono.getBase();
            if(elapsedMillis >= timechecker){
                clickFlag = 1;
                timechecker = timechecker + 5000;
                timer.cancel();
                timer.purge();
            }*/

//            if(latitude != 0.0) {
////                String timeStamp = new SimpleDateFormat("HH-mm-ss").format(new Date());
//                Long timeStamp = System.currentTimeMillis();
////                Log.i("AUG TEST", "Time elapsed between start of the video and writing data (l=0) : "+(timeStamp - timeS1)+" ms");
//                writer.println(longitude + "," + latitude + "," + speed + "," + dist[0] + "," + (timeStamp - timeS1) + "," + linear_acc_x + "," + linear_acc_y + "," + linear_acc_z + "," +
//                        heading + "," + gyro_x + "," + gyro_y + "," + gyro_z + "," + (ta_gy - timeS1) + "," + (ta_acc - timeS1) + "," + (ta_h - timeS1));
//
//            }
//            else{
//                dist[0] = (float) 0.0;
////                String timeStamp = new SimpleDateFormat("HH-mm-ss").format(new Date());
//                Long timeStamp = System.currentTimeMillis();
////                Log.i("AUG TEST", "Time elapsed between start of the video and writing data (l!=0) : "+(timeStamp - timeS1)+" ms");
//                writer.println(longitude_original + "," + latitude_original + "," + speed + "," + dist[0] + "," + timeStamp + "," + linear_acc_x + "," + linear_acc_y + "," + linear_acc_z + "," +
//                        heading + "," + gyro_x + "," + gyro_y + "," + gyro_z + "," + ta_gy + "," + ta_acc + "," + ta_h);
//





        }
    }

    public void storeData() {

//        String filePath = Environment.getExternalStorageDirectory().getPath()+"/elab/" + timeStampFile + "/" + timeStampFile  +  ".csv";
//        try {
//            writer = new PrintWriter(filePath);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

//        writer.println("Longitude" + "," + "Latitude" + "," + "Speed" + "," + "Distance" + "," + "Time" + "," + "Acc X" + "," + "Acc Y" + "," + "Acc Z" + "," + "Heading"
//                + "," + "gyro_x" + "," + "gyro_y" + "," + "gyro_z");
//        LocationManager original = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Location original_location = original.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        if(original.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
//            latitude_original = original_location.getLatitude();
//            longitude_original = original_location.getLongitude();
//        }
//        String setTextText = "Heading: " + heading + " Speed: " + speed;
//        tv.setText(setTextText);
//        timer = new Timer();
//        timer.schedule(new SayHello(), 0, rate);
        /*if(clickFlag == 1) {
            capture.performClick();
        }
        */
//        gyroTh = new SensorThread(sensorManager, Sensor.TYPE_GYROSCOPE, timeStampFile);
//        accTh = new SensorThread(sensorManager, Sensor.TYPE_ACCELEROMETER, timeStampFile);
//        gyroTh.start();
//        accTh.start();
    }

    public void enddata() {
        Log.i("MainActivity", "Stop Threads... ! ");
        gyroTh.stopTh();
        accTh.stopTh();
    }


    /* ---------------------- Sensor data ------------------- */

    private SensorManager sensorManager;

    private Sensor accelerometer;
    private Sensor head;
    private Sensor gyro;
    float linear_acc_x = 0;
    float linear_acc_y = 0;
    float linear_acc_z = 0;

    long ta_acc = 0;
    long ta_gy = 0;
    long ta_h = 0;

    float heading = 0;

    float gyro_x = 0;
    float gyro_y = 0;
    float gyro_z = 0;

//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//
//        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
//            ta_acc = System.currentTimeMillis();
//            linear_acc_x = event.values[0];
//            linear_acc_y = event.values[1];
//            linear_acc_z = event.values[2];
//        }
//        else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
//            heading = Math.round(event.values[0]);
//            ta_h = System.currentTimeMillis();
//            if(heading >= 270){
//                heading = heading + 90;
//                heading = heading - 360;
//            }
//            else{
//                heading = heading + 90;
//            }
//        }
//        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
//            ta_gy = System.currentTimeMillis();
//            gyro_x = event.values[0];
//            gyro_y = event.values[1];
//            gyro_z = event.values[2];
//        }
//        String setTextText = "Heading: " + heading + " Speed: " + speed;
//        tv.setText(setTextText);
//    }
    String[] options = {"1080p","720p","480p"};
    String[] options1 = {"15 Hz","10 Hz"};
    String[] options2 = {"10 fps","20 fps","30 fps", "60 fps", "240 fps"};


    public void addQuality(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String setting = new String();
        if(quality == 0) {
            setting = "1080p";
        }
        else if(quality == 1){
            setting = "720p";
        }
        else if(quality == 2){
            setting = "480p";
        }
        builder.setTitle("Pick Quality, Current setting: " + setting)
                .setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if(which == 0){
                            quality = 0;
                        }
                        else if (which == 1){
                            quality = 1;
                        }
                        else if (which == 2){
                            quality = 2;
                        }
                    }
                });
        builder.show();
    }
    public void addRate(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String setting = new String();
        if(rate == 100) {
            setting = "10 Hz";
        }
        else if(rate == 67){
            setting = "15 Hz";
        }
        builder.setTitle("Pick Data Save Rate, Current setting: " + setting)
                .setItems(options1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if(which == 0){
                            rate = 67 ;
                        }
                        else if (which == 1){
                            rate = 100;
                        }
                    }
                });
        builder.show();
    }
    public void addFrameRate(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String setting = new String();
        if(VideoFrameRate == 10) {
            setting = "10 fps";
        }
        else if(VideoFrameRate == 20){
            setting = "20 fps";
        }
        else if(VideoFrameRate == 30){
            setting = "30 fps";
        }
        else if(VideoFrameRate == 60){
            setting = "60 fps";
        }
        else if(VideoFrameRate == 240){
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null;
            try {
                cameraId = manager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            int id = Integer.valueOf(cameraId);
            if(CamcorderProfile.hasProfile(id,CamcorderProfile.QUALITY_HIGH_SPEED_LOW)){
                CamcorderProfile profile = CamcorderProfile.get(id, CamcorderProfile.QUALITY_HIGH_SPEED_LOW);
                int videoFrameRate = profile.videoFrameRate;
                Log.i("CAMCORDER SUC", "profil existe et vaut "+videoFrameRate+" !!");
            } else {
                Log.i("CAMCORDER PB", "le profil n'existe pas");
            }
            setting = "240 fps";
        }

        builder.setTitle("Pick Video fps, Current setting: " + setting)
                .setItems(options2, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if(which == 0){
                            VideoFrameRate = 10 ;
                        }
                        else if (which == 1){
                            VideoFrameRate = 20;
                        }
                        else if (which == 2){
                            VideoFrameRate = 30;
                        }
                        else if (which == 3){
                            VideoFrameRate = 60;
                        }
                        else if (which == 4){
                            VideoFrameRate = 240;
                        }
                    }
                });
        builder.show();
    }

    private String mCameraId;
    private SCamera mSCamera;
    private SCameraCharacteristics mCharacteristics;
    private List<VideoParameter> mVideoParameterList = new ArrayList<>();

    private boolean checkRequiredFeatures() {
        Log.i(TAG, "Enter in checkRequiredFeatures !");
        try {
            mCameraId = null;

            // Find camera device that facing to given facing parameter.
            for (String id : mSCamera.getSCameraManager().getCameraIdList()) {
                SCameraCharacteristics cameraCharacteristics = mSCamera.getSCameraManager().getCameraCharacteristics(id);
                if (cameraCharacteristics.get(SCameraCharacteristics.LENS_FACING) == SCameraCharacteristics.LENS_FACING_BACK) {
                    mCameraId = id;
                    break;
                }
            }

            if (mCameraId == null) {
                showAlertDialog("No back-facing camera exist.", true);
                Log.i(TAG, "No back-facing camera exist.");

                return false;
            }

            if (Build.VERSION.SDK_INT < 23) {
                showAlertDialog("Device running Android prior to M is not compatible with the Constrained high speed session APIs.", true);
                Log.e(TAG, "Device running Android prior to M is not compatible with the Constrained high speed session APIs.");
                Log.i(TAG, "Device running Android prior to M is not compatible with the Constrained high speed session APIs.");
                return false;
            }

            // acquires camera characteristics
            mCharacteristics = mSCamera.getSCameraManager().getCameraCharacteristics(mCameraId);

            if (!contains(mCharacteristics.get(SCameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), SCameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
                showAlertDialog("Required AF mode is not supported.", true);
                Log.i(TAG, "Required AF mode is not supported.");
                return false;
            }

            if (!contains(mCharacteristics.get(SCameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES), SCameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO) ||
                    mCharacteristics.get(SCameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getHighSpeedVideoSizes().length == 0 ||
                    mCharacteristics.get(SCameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getHighSpeedVideoFpsRanges().length == 0) {
                showAlertDialog("High speed video recording capability is not supported.", true);
                Log.i(TAG, "High speed video recording capability is not supported.");
                return false;
            }


            mVideoParameterList.clear();

            for (Size videoSize : mCharacteristics.get(SCameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getHighSpeedVideoSizes()) {
                for (Range<Integer> fpsRange : mCharacteristics.get(SCameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getHighSpeedVideoFpsRangesFor(videoSize)) {

                    //we will record constant fps video
                    if (fpsRange.getLower().equals(fpsRange.getUpper())) {
                        mVideoParameterList.add(new VideoParameter(videoSize, fpsRange));
                        Log.i(TAG, "fpsRange.getUpper() : "+fpsRange.getUpper()+" !");
                    } else {
                        Log.i(TAG, "No fpsRange.getUpper().... : "+fpsRange.getUpper()+" !");
                    }
                }
            }

        } catch (CameraAccessException e) {
            showAlertDialog("Cannot access the camera.", true);
            Log.e(TAG, "Cannot access the camera.", e);
            return false;
        }

        return true;
    }

    /**
     * Shows alert dialog.
     */
    private void showAlertDialog(String message, final boolean finishActivity) {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Alert")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (finishActivity) finish();
                    }
                }).setCancelable(false);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

    private static class VideoParameter {
        final Size mVideoSize;
        final Range<Integer> mFpsRange;

        VideoParameter(Size videoSize, Range<Integer> fpsRange) {
            mVideoSize = new Size(videoSize.getWidth(), videoSize.getHeight());
            mFpsRange = new Range<>(fpsRange.getLower(), fpsRange.getUpper());
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof VideoParameter &&
                    mVideoSize.equals(((VideoParameter) o).mVideoSize) &&
                    mFpsRange.equals(((VideoParameter) o).mFpsRange);
        }

        @Override
        public String toString() {
            return mVideoSize.toString() + " @ " + mFpsRange.getUpper() + "FPS";
        }
    }

}