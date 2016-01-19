package djavid.coursework;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class SensorActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private float[] rotation;       //матрица поворота
    private float[] accelerometer;  //данные с акселерометра
    private float[] magnet;         //данные с геомагнитного датчика
    private float[] orientation;    //матрица положения в пространстве

    private TextView viewX;
    private TextView viewY;
    private TextView viewZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); // Получаем менеджер сенсоров

        rotation = new float[16];
        accelerometer = new float[3];
        magnet = new float[3];
        orientation = new float[3];

        viewX = (TextView) findViewById(R.id.valueX);
        viewY = (TextView) findViewById(R.id.valueY);
        viewZ = (TextView) findViewById(R.id.valueZ);
    }

    private void getData(SensorEvent event) {
        final int type = event.sensor.getType();

        if(type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometer, 0, 3);
        }

        if(type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnet, 0, 3);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        getData(event);

        SensorManager.getRotationMatrix(rotation, null, accelerometer, magnet); //получаем матрицу поворота
        SensorManager.getOrientation(rotation, orientation); //получаем данные ориентации устройства в пространстве

        viewX.setText(String.valueOf(Math.round(Math.toDegrees(orientation[0]))));
        viewY.setText(String.valueOf(Math.round(Math.toDegrees(orientation[1]))));
        viewZ.setText(String.valueOf(Math.round(Math.toDegrees(orientation[2]))));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
