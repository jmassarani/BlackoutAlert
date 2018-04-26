package com.example.fmohe.blackoutalertapp;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

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
    private Button prompt;

    //Writing to files
    private String dir = Environment.getExternalStorageDirectory().toString() + "/BlackoutAlertData/";
    private File currDataFile;
    private FileOutputStream fileOutputStream;
    private RadioGroup radioGroup;

    private String prompt_text;
    private ArrayList<String> words = new ArrayList<String>(){{
        add("fork");
        add("general");
        add("axiomatic");
        add("desk");
        add("desire");
        add("bewildered");
        add("curl");
        add("frequent");
        add("profit");
        add("tie");
        add("healthy");
        add("billowy");
        add("cough");
        add("insidious");
        add("lumpy");
        add("kiss");
        add("plate");
        add("overt");
        add("shelf");
        add("accessible");
        add("regret");
        add("crack");
        add("experience");
        add("fold");
        add("fascinated");
        add("limit");
        add("curve");
        add("type");
        add("x-ray");
        add("bikes");
        add("clumsy");
        add("border");
        add("untidy");
        add("license");
        add("pin");
        add("absurd");
        add("taboo");
        add("arrive");
        add("spring");
        add("offer");
        add("unsuitable");
        add("low");
        add("fresh");
        add("tasteless");
        add("trains");
        add("thoughtless");
        add("drink");
        add("better");
        add("deliver");
        add("grip");
        add("pass");
        add("cold");
        add("rat");
        add("naive");
        add("explode");
        add("abrupt");
        add("argue");
        add("step");
        add("property");
        add("suit");
        add("fearless");
        add("reaction");
        add("well-groomed");
        add("change");
        add("apologise");
        add("stomach");
        add("raspy");
        add("nimble");
        add("burn");
        add("pet");
        add("lyrical");
        add("pen");
        add("provide");
        add("living");
        add("illegal");
        add("territory");
        add("present");
        add("efficient");
        add("yam");
        add("eyes");
        add("oceanic");
        add("plug");
        add("price");
        add("permissible");
        add("elated");
        add("hungry");
        add("receive");
        add("scarce");
        add("guess");
        add("shut");
        add("scent");
        add("tangible");
        add("kitty");
        add("quickest");
        add("royal");
        add("demonic");
        add("army");
        add("copy");
        add("jump");
        add("listen");
    }};

    //Rounds to number of places
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //Gets three random words from words list
    public String threeRandom() {
        String accum = "";
        for (int i = 0; i < 3; i++) {
            Random gen = new Random();
            int index = gen.nextInt(words.size());
            accum += words.get(index);
            if (i < 2) {
                accum += " ";
            }
        }

        return accum;
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
            case R.id.promptUser:
                //Stop collecting sensor
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

                //Get three random words
                final String three_words = threeRandom();

                //Prompt user
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Type this: " + three_words);

                // Set up the input
                final EditText input = new EditText(this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prompt_text = input.getText().toString();

                        String d = three_words + "\n" + prompt_text;
                        //Save user output to file
                        String cal = Calendar.getInstance().getTime().toString();
                        cal = cal.replaceAll(" ", "_").toLowerCase();
                        File f = new File(dir,cal + "_USER_PROMPTS.txt");
                        try {
                            FileOutputStream stream = new FileOutputStream(f);
                            stream.write(d.getBytes());
                            stream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

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
        prompt = (Button) findViewById(R.id.promptUser);

        //Button listeners
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        prompt.setOnClickListener(this);

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
