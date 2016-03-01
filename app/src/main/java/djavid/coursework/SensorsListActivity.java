package djavid.coursework;

import android.app.ListActivity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class SensorsListActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> sensorsList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        List<String> sensorsListName = new ArrayList<>();

        for (int i = 0; i < sensorsList.size(); i++) {
            sensorsListName.add(sensorsList.get(i).getName() +
                    " (" + sensorsList.get(i).getStringType() + ")");
        }

        setListAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, sensorsListName));
        getListView().setTextFilterEnabled(true);
    }

}
