package com.bhupadhy.temperature;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
	// Static Variables
	public final static String TAG = "MainActivity";
	public final static String CelsiusSym = "\u2103";
	public final static String FahrenheitSym = "\u2109";
	public final static int NumOfDays = 5;

	// Sensor Variables
	SensorManager sManager;
	Sensor ambientTempSensor;
	float ambientTempValue;
	boolean hasSensor;

	// None of my phones have a ambient temp sensor
	// And you cant test sensors on an emulator but you could spoof the sensor data through cl
	// So I am using battery temp to test all the other function of the application.
	BroadcastReceiver batteryInfoReceiver;

	// Current Scale Variable initially in Celsius
	char scale = 'C';

	// Random Temperatures for Mon -> Fri
	float[] temperatures;

	// TextView/Button UI
	TextView ambText;
	TextView monText;
	TextView tueText;
	TextView wedText;
	TextView thuText;
	TextView friText;
	Button convertButton;


	// JNI Functions
	public native float ConvertTemp(float temp, char scale);

	public native float[] ConvertListTemps(float[] temps, char scale);

	// Load JNI Libraries
	static {
		System.loadLibrary("temperature");
	}

	// Life cycle methods
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUI();
		initializeSensor();
		initializeTemperatures();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Reregister
		if (hasSensor) registerListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Have to unregister listener onPause to save battery
		if (hasSensor) unregisterListener();
	}

	// Initialize TextViews / Buttons / OnClickListener
	private void initUI() {
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ambText = (TextView) findViewById(R.id.ambient_value_text);
		monText = (TextView) findViewById(R.id.monday_value_text);
		tueText = (TextView) findViewById(R.id.tuesday_value_text);
		wedText = (TextView) findViewById(R.id.wednesday_value_text);
		thuText = (TextView) findViewById(R.id.thursday_value_text);
		friText = (TextView) findViewById(R.id.friday_value_text);
		convertButton = (Button) findViewById(R.id.button_switch_scale);
		convertButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleScale();
				temperatures = ConvertListTemps(temperatures, scale);
				updateAmbientTempUI();
				updateTemperatureView();
			}
		});
	}

	public void updateTemperatureView() {
		if (temperatures.length < NumOfDays) return;
		monText.setText(formatTemp(temperatures[0]));
		tueText.setText(formatTemp(temperatures[1]));
		wedText.setText(formatTemp(temperatures[2]));
		thuText.setText(formatTemp(temperatures[3]));
		friText.setText(formatTemp(temperatures[4]));
	}

	public void updateAmbientTempUI() {
		String result;
		if (scale == 'C') {
			result = ambientTempValue + " " + CelsiusSym;

		} else {
			result = convertTemp(ambientTempValue, 'F');
		}
		ambText.setText(result);
	}

	/*
	 * Sensor
	 */
	public void initializeSensor() {
		sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (checkAmbientTempSensor()) {
			ambientTempSensor = sManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
			// Register Sensor Event Listeners
			registerListener();
		} else { // Use battery temperature if there is no ambient temperature sensor
			Toast.makeText(this, "No ambient temperature sensor detected. Using battery temperature", Toast.LENGTH_SHORT).show();
			batteryInfoReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					// Divide by 10 because temperature is in tenths of a degree centigrade
					ambientTempValue = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10;
					updateAmbientTempUI();
				}
			};
			this.registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		ambientTempValue = event.values[0];
		updateAmbientTempUI();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.i(TAG, "Sensor Accuracy changed to " + accuracy);
	}

	// Check if Ambient Temperature sensor is available on the device
	public boolean checkAmbientTempSensor() {
		if (sManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE).size() > 0) {
			hasSensor = true;
		}
		return hasSensor;
	}

	public void registerListener() {
		sManager.registerListener(this, ambientTempSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void unregisterListener() {
		sManager.unregisterListener(this);
	}

	// Utility functions to make the strings look a little more pretty
	// This is where the JNI ConvertTemp gets called and the return float
	// value is then formatted into a presentable string and returned
	public String convertTemp(float val, char scale) {
		return formatTemp(ConvertTemp(val, scale));
	}

	public String formatTemp(float val) {
		StringBuilder result = new StringBuilder();
		result.append(String.format("%f", val));
		if (scale == 'C') {
			result.append(" " + CelsiusSym);

		} else {
			result.append(" " + FahrenheitSym);
		}
		return result.toString();
	}


	public void initializeTemperatures() {
		temperatures = new float[NumOfDays];
		randomizeTemperatures();
		updateTemperatureView();
	}

	// Generate random temperatures for 5 days (Mon->Fri) in the range of -20 to 200
	// degrees Celsius
	public void randomizeTemperatures() {
		Random rand = new Random();
		float minVal = -20.0f;
		float maxVal = 200.0f;
		for (int i = 0; i < NumOfDays; i++) {
			temperatures[i] = (rand.nextFloat() * (maxVal - minVal) + minVal);
		}
	}


	// Changes temperature scale to opposite of what it currently is
	public void toggleScale() {
		scale = scale == 'C' ? 'F' : 'C';
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


}
