package edu.wpi.activityrecognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener, Listener {


    private TextView textView;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    private int numSteps;


    private String TAG = MainActivity.class.getSimpleName();
    BroadcastReceiver broadcastReceiver;

    private TextView txtActivity, txtConfidence;
    private ImageView imgActivity;

    private TextView TvSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtActivity = findViewById(R.id.user_status);
        imgActivity = findViewById(R.id.img_activity);


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };
         TvSteps = findViewById(R.id.tv_steps);

        startTracking();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        numSteps = 0;
        sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);
    }



    private void handleUserActivity(int type, int confidence) {
        String label = getString(R.string.activity_unknown);

        switch (type) {
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                break;
            }
        }

        Log.e(TAG, "User activity: " + label + ", Confidence: " + confidence);

        if (confidence > Constants.CONFIDENCE) {
            if(label == getString(R.string.activity_still)){
                txtActivity.setText(label);
                imgActivity.setImageResource(R.drawable.still);
            }
            else if(label == getString(R.string.activity_walking)){
                txtActivity.setText(label);
                imgActivity.setImageResource(R.drawable.walking);
            }
            else if(label == getString(R.string.activity_running)){
                txtActivity.setText(label);
                imgActivity.setImageResource(R.drawable.running);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
        startTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void startTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent);
    }

    private void stopTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopTracking();
    }

    @Override
    protected void onStop(){
        super.onStop();
        stopTracking();
    }

}
