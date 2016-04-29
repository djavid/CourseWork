package djavid.coursework;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.ListActivity;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    //private final static String FILE_DATA = "data.txt";
    private final static String DIRECTORYSD = "Sensors";
    private String HEADER;
    private String FILE_NAME;
    private File sdPath;
    private CLASS_LABEL classLabel;

    private SensorManager mSensorManager;

    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagneticField;
    private Sensor mGravity;
    private Sensor mLinearAcceleration;
    private Sensor mRotationVector;

    private float[] rotation;       //матрица поворота
    private float[] orientation;    //матрица положения в пространстве

    private float[] accelerometer;  //данные с акселерометра
    private float[] magneticField;  //данные с геомагнитного датчика
    private float[] gyroscope;
    private float[] gravity;
    private float[] linearAcceleration;
    private float[] rotationVector;

    private TextView viewAccelerometer;
    private TextView viewMagneticField;
    private TextView viewGyroscope;
    private TextView viewGravity;
    private TextView viewLinearAcceleration;
    private TextView viewRotationVector;

    private File fileData;
    private boolean startedSaving;
    private int sensorCount = 0;

    private String oldData = "";

    enum CLASS_LABEL {
        RUN, WALK, TRAVEL, GRAB
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startedSaving = false;
        findViewById(R.id.button_stop).setVisibility(View.INVISIBLE);

        final Chronometer chrono = (Chronometer) findViewById(R.id.chronometer);
        chrono.setVisibility(View.INVISIBLE);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); // Получаем менеджер сенсоров

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        rotation = new float[16];
        orientation = new float[3];

        accelerometer = new float[3];
        magneticField = new float[3];
        gyroscope = new float[3];
        gravity = new float[3];
        linearAcceleration = new float[3];
        rotationVector = new float[3];

        viewAccelerometer = (TextView) findViewById(R.id.valueAccelerometer);
        viewMagneticField = (TextView) findViewById(R.id.valueMagneticField);
        viewGyroscope = (TextView) findViewById(R.id.valueGyroscope);
        viewGravity = (TextView) findViewById(R.id.valueGravity);
        viewLinearAcceleration = (TextView) findViewById(R.id.valueLinearAcceleration);
        viewRotationVector = (TextView) findViewById(R.id.valueRotationVector);

//        HEADER = "\"ones\",\"acc_x\",\"acc_y\",\"acc_z\",\"mag_x\",\"mag_y\",\"mag_z\",\"gyr_x\",\"gyr_y\",\"gyr_z\"," +
//                "\"grav_x\",\"grav_y\",\"grav_z\",\"lin_x\",\"lin_y\",\"lin_z\",\"rot_x\",\"rot_y\",\"rot_z\",\"label\"\n";
        HEADER = "\"ones\",\"acc_x\",\"acc_y\",\"acc_z\",\"gyr_x\",\"gyr_y\",\"gyr_z\",\"label\"\n";

        classLabel = CLASS_LABEL.WALK;

        sdPath = Environment.getExternalStorageDirectory();
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIRECTORYSD);

        if(!sdPath.exists()) {
            sdPath.mkdirs();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sensor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            saveFilesButton();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getData(SensorEvent event) {
        final int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometer, 0, 3);
        }

        if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticField, 0, 3);
        }

        if (type == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, gyroscope, 0, 3);
        }

        if (type == Sensor.TYPE_GRAVITY) {
            System.arraycopy(event.values, 0, gravity, 0, 3);
        }

        if (type == Sensor.TYPE_LINEAR_ACCELERATION) {
            System.arraycopy(event.values, 0, linearAcceleration, 0, 3);
        }

        if (type == Sensor.TYPE_ROTATION_VECTOR) {
            System.arraycopy(event.values, 0, rotationVector, 0, 3);
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorCount += 1;
        getData(event);

        SensorManager.getRotationMatrix(rotation, null, accelerometer, magneticField); //получаем матрицу поворота
        SensorManager.getOrientation(rotation, orientation); //получаем данные ориентации устройства в пространстве

//        String allData = "1" + "," + accelerometer[0] + "," +accelerometer[1] + "," +accelerometer[2] + "," +
//                magneticField[0] + "," + magneticField[1] + "," + magneticField[2] + "," +
//                gyroscope[0] + "," + gyroscope[1] + "," + gyroscope[2] + "," +
//                gravity[0] + "," + gravity[1] + "," + gravity[2] + "," +
//                linearAcceleration[0] + "," + linearAcceleration[1] + "," + linearAcceleration[2] + "," +
//                rotationVector[0] + "," + rotationVector[1] + "," + rotationVector[2] + "," +
//                classLabel.ordinal() + "\n";
        String allData = "1" + "," +
                accelerometer[0] + "," +accelerometer[1] + "," +accelerometer[2] + "," +
                gyroscope[0] + "," + gyroscope[1] + "," + gyroscope[2] + "," +
                classLabel.ordinal() + "\n";

        if(startedSaving) {
            saveToFile(allData);
        }

        //output
        String str, strFinal;

        str = getResources().getString(R.string.value_accelerometer);
        strFinal = String.format(str,
                accelerometer[0],
                accelerometer[1],
                accelerometer[2]);
        viewAccelerometer.setText(strFinal);

        str = getResources().getString(R.string.value_magnetic_field);
        strFinal = String.format(str,
                magneticField[0],
                magneticField[1],
                magneticField[2]);
        viewMagneticField.setText(strFinal);

        str = getResources().getString(R.string.value_gyroscope);
        strFinal = String.format(str,
                gyroscope[0],
                gyroscope[1],
                gyroscope[2]);
        viewGyroscope.setText(strFinal);

        str = getResources().getString(R.string.value_gravity);
        strFinal = String.format(str,
                gravity[0],
                gravity[1],
                gravity[2]);
        viewGravity.setText(strFinal);

        str = getResources().getString(R.string.value_linear_acceleration);
        strFinal = String.format(str,
                linearAcceleration[0],
                linearAcceleration[1],
                linearAcceleration[2]);
        viewLinearAcceleration.setText(strFinal);

        str = getResources().getString(R.string.value_rotation_vector);
        strFinal = String.format(str,
                rotationVector[0],
                rotationVector[1],
                rotationVector[2]);
        viewRotationVector.setText(strFinal);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL); //SENSOR_DELAY_UI
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);

        viewAccelerometer.setVisibility(View.VISIBLE);
        viewMagneticField.setVisibility(View.VISIBLE);
        viewGyroscope.setVisibility(View.VISIBLE);
        viewGravity.setVisibility(View.VISIBLE);
        viewLinearAcceleration.setVisibility(View.VISIBLE);
        viewRotationVector.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    private void saveToFile(String values) {
        if (!values.equals(oldData)) {
            oldData = values;

            try {
                BufferedWriter bw;

                if (!fileData.exists()) {
                    fileData.createNewFile();
                }

                bw = new BufferedWriter(new FileWriter(fileData, true));
                bw.append(values);
                bw.flush();
                bw.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveFilesButton() {
        AlertDialog.Builder alert = new AlertDialog.Builder(SensorActivity.this);
        alert.setTitle("Choose file name");

        final View view = getLayoutInflater().inflate(R.layout.alert_dialog, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        final Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                classLabel = CLASS_LABEL.valueOf((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        alert.setView(view);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FILE_NAME = input.getText().toString();
                FILE_NAME.trim();
                FILE_NAME += ".txt";

                fileData = new File(sdPath, FILE_NAME);
                try {
                    if (fileData.exists()) {
                        fileData.delete();
                    }

                    fileData.createNewFile();
                    saveToFile(HEADER);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!startedSaving) {
                    Chronometer chrono = (Chronometer) findViewById(R.id.chronometer);
                    chrono.setVisibility(View.VISIBLE);
                    findViewById(R.id.button_stop).setVisibility(View.VISIBLE);

                    startedSaving = true;
                    chrono.setBase(SystemClock.elapsedRealtime());
                    chrono.start();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alert.show();
    }

    public void stopSavingFiles(View view) {
        if(startedSaving) {
            startedSaving = false;
            Chronometer chrono = (Chronometer) findViewById(R.id.chronometer);
            chrono.stop();
            chrono.setBase(SystemClock.elapsedRealtime());

            findViewById(R.id.button_stop).setVisibility(View.INVISIBLE);
        }
    }


}
