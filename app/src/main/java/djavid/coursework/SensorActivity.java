package djavid.coursework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.app.ListActivity;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private final static String FILE_ACCELEROMETER = "accelerometer.txt";
    private final static String FILE_MAGNETIC_FIELD = "magnetic_field.txt";
    private final static String FILE_GYROSCOPE = "gyroscope.txt";
    private final static String FILE_PROXIMITY = "proximity.txt";
    private final static String FILE_GRAVITY = "gravity.txt";
    private final static String FILE_LINEAR_ACCELERATION = "linear_acceleration.txt";
    private final static String FILE_ROTATION_VECTOR = "rotation_vector.txt";

    private String sAccelerometer = "";
    private String sMagneticField = "";
    private String sGyroscope = "";
    private String sProximity = "";
    private String sGravity = "";
    private String sLinearAcceleration = "";
    private String sRotationVector = "";

    private final static String DIRECTORYSD = "Sensors";

    private SensorManager mSensorManager;

    private Sensor mAccelerometer;
    private Sensor mMagneticField;
    private Sensor mGyroscope;
    private Sensor mProximity;
    private Sensor mGravity;
    private Sensor mLinearAcceleration;
    private Sensor mRotationVector;

    private float[] rotation;       //матрица поворота
    private float[] orientation;    //матрица положения в пространстве

    private float[] accelerometer;  //данные с акселерометра
    private float[] magneticField;         //данные с геомагнитного датчика
    private float[] gyroscope;
    private float proximity;
    private float[] gravity;
    private float[] linearAcceleration;
    private float[] rotationVector;


    private TextView viewAccelerometer;
    private TextView viewMagneticField;
    private TextView viewGyroscope;
    private TextView viewProximity;
    private TextView viewGravity;
    private TextView viewLinearAcceleration;
    private TextView viewRotationVector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); // Получаем менеджер сенсоров

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
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
        viewProximity = (TextView) findViewById(R.id.valueProximity);
        viewGravity = (TextView) findViewById(R.id.valueGravity);
        viewLinearAcceleration = (TextView) findViewById(R.id.valueLinearAcceleration);
        viewRotationVector = (TextView) findViewById(R.id.valueRotationVector);
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
            saveFiles();
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

        if (type == Sensor.TYPE_PROXIMITY) {
            proximity = event.values[0];
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
        getData(event);

        SensorManager.getRotationMatrix(rotation, null, accelerometer, magneticField); //получаем матрицу поворота
        SensorManager.getOrientation(rotation, orientation); //получаем данные ориентации устройства в пространстве

        viewAccelerometer.setVisibility(View.VISIBLE);
        viewMagneticField.setVisibility(View.VISIBLE);
        viewGyroscope.setVisibility(View.VISIBLE);
        viewProximity.setVisibility(View.VISIBLE);
        viewGravity.setVisibility(View.VISIBLE);
        viewLinearAcceleration.setVisibility(View.VISIBLE);
        viewRotationVector.setVisibility(View.VISIBLE);

        String str, strFinal;

        str = getResources().getString(R.string.value_accelerometer);
        strFinal = String.format(str,
                accelerometer[0],
                accelerometer[1],
                accelerometer[2]);
        viewAccelerometer.setText(strFinal);
        sAccelerometer += String.format("x: %1$.4f; y: %2$.4f; z: %3$.4f \n",
                accelerometer[0],
                accelerometer[1],
                accelerometer[2]);

        str = getResources().getString(R.string.value_magnetic_field);
        strFinal = String.format(str,
                magneticField[0],
                magneticField[1],
                magneticField[2]);
        viewMagneticField.setText(strFinal);
        sMagneticField += String.format("x: %1$.4f; y: %2$.4f; z: %3$.4f \n",
                magneticField[0],
                magneticField[1],
                magneticField[2]);

        str = getResources().getString(R.string.value_gyroscope);
        strFinal = String.format(str,
                gyroscope[0],
                gyroscope[1],
                gyroscope[2]);
        viewGyroscope.setText(strFinal);
        sGyroscope += String.format("x: %1$.4f; y: %2$.4f; z: %3$.4f \n",
                gyroscope[0],
                gyroscope[1],
                gyroscope[2]);

        str = getResources().getString(R.string.value_proximity);
        strFinal = String.format(str, proximity);
        viewProximity.setText(strFinal);
        sProximity += String.format("distance: %1$f \n", proximity);

        str = getResources().getString(R.string.value_gravity);
        strFinal = String.format(str,
                gravity[0],
                gravity[1],
                gravity[2]);
        viewGravity.setText(strFinal);
        sGravity += String.format("x: %1$.4f; y: %2$.4f; z: %3$.4f \n",
                gravity[0],
                gravity[1],
                gravity[2]);

        str = getResources().getString(R.string.value_linear_acceleration);
        strFinal = String.format(str,
                linearAcceleration[0],
                linearAcceleration[1],
                linearAcceleration[2]);
        viewLinearAcceleration.setText(strFinal);
        sLinearAcceleration += String.format("x: %1$.4f; y: %2$.4f; z: %3$.4f \n",
                linearAcceleration[0],
                linearAcceleration[1],
                linearAcceleration[2]);

        str = getResources().getString(R.string.value_rotation_vector);
        strFinal = String.format(str,
                rotationVector[0],
                rotationVector[1],
                rotationVector[2]);
        viewRotationVector.setText(strFinal);
        sRotationVector += String.format("x: %1$.4f; y: %2$.4f; z: %3$.4f \n",
                rotationVector[0],
                rotationVector[1],
                rotationVector[2]);
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
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private void saveFiles() {
        File sdPath = Environment.getExternalStorageDirectory();
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIRECTORYSD);
        if(!sdPath.exists()) {
            sdPath.mkdirs();
        }

        File fileAccelerometer = new File(sdPath, FILE_ACCELEROMETER);
        File fileMagneticField = new File(sdPath, FILE_MAGNETIC_FIELD);
        File fileGyroscope = new File(sdPath, FILE_GYROSCOPE);
        File fileProximity = new File(sdPath, FILE_PROXIMITY);
        File fileGravity = new File(sdPath, FILE_GRAVITY);
        File fileLinearAcceleration = new File(sdPath, FILE_LINEAR_ACCELERATION);
        File fileRotationVector = new File(sdPath, FILE_ROTATION_VECTOR);

        try {
            String string = "Hello world!";
            BufferedWriter bw;

            fileAccelerometer.createNewFile();
            fileMagneticField.createNewFile();
            fileGyroscope.createNewFile();
            fileProximity.createNewFile();
            fileGravity.createNewFile();
            fileLinearAcceleration.createNewFile();
            fileRotationVector.createNewFile();

            bw = new BufferedWriter(new FileWriter(fileAccelerometer));
            bw.write(sAccelerometer);
            bw.flush();
            bw.close();

            bw = new BufferedWriter(new FileWriter(fileMagneticField));
            bw.write(sMagneticField);
            bw.flush();
            bw.close();

            bw = new BufferedWriter(new FileWriter(fileGyroscope));
            bw.write(sGyroscope);
            bw.flush();
            bw.close();

            bw = new BufferedWriter(new FileWriter(fileProximity));
            bw.write(sProximity);
            bw.flush();
            bw.close();

            bw = new BufferedWriter(new FileWriter(fileGravity));
            bw.write(sGravity);
            bw.flush();
            bw.close();

            bw = new BufferedWriter(new FileWriter(fileLinearAcceleration));
            bw.write(sLinearAcceleration);
            bw.flush();
            bw.close();

            bw = new BufferedWriter(new FileWriter(fileRotationVector));
            bw.write(sRotationVector);
            bw.flush();
            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
