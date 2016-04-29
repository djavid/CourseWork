package djavid.coursework;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;


public class RecogniseActivity extends AppCompatActivity implements SensorEventListener {
    private enum ActivityEnum {
        RUNNING, WALKING, GRABBING, TRAVELLING, DEFAULT;
    }
    List<ActivityEnum> results;

    private final static int SEC = 5;

    private int dataCount;
    private int secCount;
    private boolean started;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private float[] accelerometer;  //данные с акселерометра
    private float[] gyroscope;
    private float[][] testData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognise);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); // Получаем менеджер сенсоров
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        accelerometer = new float[3];
        gyroscope = new float[3];
        testData = new float[20][];
        dataCount = 0;
        secCount = 0;
        started = false;
        results = new ArrayList<>(SEC);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        getSensorData(event);

        if (started) {
            testData[dataCount] = new float[6];
            System.arraycopy(accelerometer, 0, testData[dataCount], 0, 3);
            System.arraycopy(gyroscope, 0, testData[dataCount], 3, 3);

            dataCount += 1;
            if (dataCount == 20) {
                DataLine dataLine = new DataLine(testData);
                predictData(dataLine);

                testData = new float[20][];
                dataCount = 0;
                secCount += 1;

                if (secCount == SEC) {
                    ActivityEnum result = getResultActivity();
                    ((TextView) findViewById(R.id.textview_result)).setText(result.toString());
                    toggleRecognising(getCurrentFocus());
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private ActivityEnum getResultActivity() {
        int[] values = new int[4];

        for (int i = 0; i< results.size(); i++) {
            values[results.get(i).ordinal()]++;
        }

        ActivityEnum result = ActivityEnum.DEFAULT;
        int max = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];

                switch (i) {
                    case 0: result = ActivityEnum.RUNNING; break;
                    case 1: result = ActivityEnum.WALKING; break;
                    case 2: result = ActivityEnum.GRABBING; break;
                    case 3: result = ActivityEnum.TRAVELLING; break;
                }
            }
        }

        return result;
    }

    public void toggleRecognising(View view) {
        final Button button = (Button) findViewById(R.id.button_toggle);
        final TextView timer = (TextView) findViewById(R.id.textview_timer);
        final TextView result = (TextView) findViewById(R.id.textview_result);

        if(!started) {


            final AlertDialog.Builder builder = new AlertDialog.Builder(RecogniseActivity.this);
            builder.setMessage("Please put phone in your pocket for better activity recognision!")
                    .setCancelable(false)
                    .setNegativeButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();

                                    String str = getResources().getString(R.string.waiting_activity);
                                    result.setText(str);
                                    timer.setVisibility(View.VISIBLE);
                                    button.setVisibility(View.INVISIBLE);

                                    new CountDownTimer(5000, 1000) {
                                        public void onTick(long millisUntilFinished) {
                                            String str = getResources().getString(R.string.value_prediction_timer);
                                            str = String.format(str, millisUntilFinished / 1000);
                                            timer.setText(str);
                                        }

                                        public void onFinish() {
                                            button.setText(R.string.button_stop_recognising);
                                            button.setVisibility(View.VISIBLE);
                                            timer.setText("");
                                            started = true;
                                        }
                                    }.start();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            started = false;
            testData = new float[20][];
            dataCount = 0;
            secCount = 0;
            results = new ArrayList<>(SEC);
            button.setText(R.string.button_start_recognising);
        }
    }

    private void getSensorData(SensorEvent event) {
        final int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometer, 0, 3);
        }

        if (type == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, gyroscope, 0, 3);
        }
    }

    private void predictData(DataLine data) {
        float[] calcData = data.calcMeanStd();
        ActivityEnum result = LogisticFunc(calcData);
        results.add(result);

//        TextView textview_recognise = (TextView) findViewById(R.id.textview_recognise);
//        String str = getResources().getString(R.string.value_prediction_results);
//        str = String.format(str, res[0], res[1], res[2], res[3]);
//        textview_recognise.setText(str);
    }

    private ActivityEnum LogisticFunc(float[] x) {
        double[] running_coefs = {0.01267182,  0.71138967, -0.65853997,  1.89211112, -0.93298528,
                -1.62088919, -0.33291263, -0.52622248,  1.74231731,  0.87471674,
                1.19506909,  2.16136385};
        double[] walking_coefs = {-0.45984535, -1.84454747, -0.29957083, -1.01880899, -0.2651113 ,
                2.60318555,  1.0066322 ,  3.52762168, -0.83807882,  1.18115188,
                -0.88558361, -0.84904903};
        double[] grabbing_coefs = {0.04679318,  0.38174699, -0.76631404,  0.19592993,  0.39496427,
                -0.81739293,  0.69225003,  0.81348456,  0.35810063,  0.17092031,
                -0.05777661, -0.40376113};
        double[] travelling_coefs = {0.48806307,  1.19776965,  0.36921832, -1.37187791, -0.0329723 ,
                -0.26578405, -1.28138674, -2.76602861, -0.73512805, -1.6811392 ,
                0.46027152, -1.08454145};

        double[] prediction = new double[4];

        double z = 0;
        for (int i = 0; i < running_coefs.length; i++) {
            z += running_coefs[i] * x[i];
        }
        prediction[0] = 1.0 / (1.0 + Math.exp(-z));

        z = 0;
        for (int i = 0; i < walking_coefs.length; i++) {
            z += walking_coefs[i] * x[i];
        }
        prediction[1] = 1.0 / (1.0 + Math.exp(-z));

        z = 0;
        for (int i = 0; i < grabbing_coefs.length; i++) {
            z += grabbing_coefs[i] * x[i];
        }
        prediction[2] = 1.0 / (1.0 + Math.exp(-z));

        z = 0;
        for (int i = 0; i < travelling_coefs.length; i++) {
            z += travelling_coefs[i] * x[i];
        }
        prediction[3] = 1.0 / (1.0 + Math.exp(-z));

        double max = 0;
        ActivityEnum activity = ActivityEnum.DEFAULT;
        for (int i = 0; i < prediction.length; i++) {
            if (prediction[i] > max) {
                max = prediction[i];
                switch (i) {
                    case 0: activity = ActivityEnum.RUNNING; break;
                    case 1: activity = ActivityEnum.WALKING; break;
                    case 2: activity = ActivityEnum.GRABBING; break;
                    case 3: activity = ActivityEnum.TRAVELLING; break;
                }
            }
        }

        return activity;
    }

    private class DataLine {
        private float[][] testData;

        public DataLine(float[][] arr) {
            testData = new float[arr.length][];
            System.arraycopy(arr, 0, testData, 0, arr.length);
        }

        public float[] calcMeanStd() {
            float[] calcData = new float[testData[0].length * 2];
            int j = 0;
            for (int i = 0; i < calcData.length; i += 2) {
                calcData[i] = calcMean(j);
                j += 1;
            }

            j = 0;
            for (int i = 1; i < calcData.length; i += 2) {
                calcData[i] = calcStd(j);
                j += 1;
            }

            return calcData;
        }

        private float calcMean(int col) {
            float mean = 0;
            for (float[] item : testData) {
                mean += item[col];
            }

            return  mean / (float)testData.length;
        }

        private float calcStd(int col) {
            float mean = calcMean(col);
            float sum = 0;

            for (float[] item : testData) {
                sum += Math.pow(item[col] - mean, 2);
            }
            sum /= (float)testData.length;

            return (float)Math.sqrt(sum);
        }
    }
}
