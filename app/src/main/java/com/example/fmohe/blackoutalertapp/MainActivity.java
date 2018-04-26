package com.example.fmohe.blackoutalertapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager sensorManager;

    //Accel variables
    private Sensor accelerometer;
    private double a_x;
    private double a_y;
    private double a_z;
    private TextView accelData;

    //Gyroscope variables
    private Sensor gyroscope;
    private double g_x;
    private double g_y;
    private double g_z;
    private TextView gyroData;

    //Buttons
    private Button startButton;
    private Button stopButton;

    //Writing to files
    private String dir = Environment.getExternalStorageDirectory().toString() + "/BlackoutAlertData/";
    private File currDataFile;
    private FileOutputStream fileOutputStream;
    private RadioGroup radioGroup;

    //Rounds to number of places
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //On click listener for the buttons
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startButton:
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);

                //Create file
                String name = Calendar.getInstance().getTime().toString();
                name = name.replaceAll(" ", "_").toLowerCase();

                //Get file name input
                int selectedId = radioGroup.getCheckedRadioButtonId();
                RadioButton curr = (RadioButton) findViewById(selectedId);
                String text = curr.getText().toString();
                name = name + "_" + text;

                currDataFile = new File(dir, name + ".txt");
                try {
                    fileOutputStream = new FileOutputStream(currDataFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.stopButton:
                sensorManager.unregisterListener(this, accelerometer);
                sensorManager.unregisterListener(this, gyroscope);
                accelData.setText("Stopped!");
                gyroData.setText("Stopped!");

                //Close file
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Sensor setup
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        accelData = (TextView) findViewById(R.id.accelData);
        gyroData = (TextView) findViewById(R.id.gyroData);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        radioGroup = (RadioGroup) findViewById(R.id.radioButtons);

        //Button listeners
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

        //Create files directory
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.toString() + "/BlackoutAlertData");
            if (!dir.exists()) {
                dir.mkdir();
            }
        }
    }

    public void onSensorChanged(SensorEvent event) {
        Sensor sens = event.sensor;

        //Check type of sensor
        if (sens.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Get the three event values
            a_x = event.values[0];
            a_y = event.values[1];
            a_z = event.values[2];

            accelData.setText("{" + round(a_x, 4) + ", " + round(a_y, 4) + ", " + round(a_z, 4) + "}\n");
        }

        if (sens.getType() == Sensor.TYPE_GYROSCOPE) {
            //Get the three event values
            g_x = event.values[0];
            g_y = event.values[1];
            g_z = event.values[2];

            gyroData.setText("{" + round(g_x, 4) + ", " + round(g_y, 4) + ", " + round(g_z, 4) + "}\n");
        }

        //Write to file
        String data = accelData.getText() + "|" + gyroData.getText();
        try {
            fileOutputStream.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    //Override from SensorEventListener class
    public void onPause() {
        super.onPause();
    }

    @Override
    //Override from SensorEventListener class
    public void onResume() {
        super.onResume();
    }

    @Override
    //Override from SensorEventListener class
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
