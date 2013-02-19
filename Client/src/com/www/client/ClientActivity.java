package com.www.client;
import java.io.File;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
public class ClientActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
	private static final String TAG = "ClientActivity";
	private static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
	private static final String CLIENT = SDCARD + "/Client";
	public static boolean restartService = false;
	SeekBar seekBar;
	TextView statusText;
	TextView levelText;
	TextView commentText;
	SharedPreferences sharedPreferences;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_client);
		seekBar = (SeekBar)findViewById(R.id.levelService);
		seekBar.setOnSeekBarChangeListener(this);
		statusText = (TextView)findViewById(R.id.status);
		levelText = (TextView)findViewById(R.id.level);
		commentText = (TextView)findViewById(R.id.comment);
		Button ok = (Button)findViewById(R.id.okButton);
		Button cancel = (Button)findViewById(R.id.cancelButton);
		ok.setOnClickListener(okListener);
		cancel.setOnClickListener(cancelListener);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		if(sharedPreferences.getBoolean("firstTime", true)) {
			new File(CLIENT).mkdir();
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("firstTime", false);
			editor.putInt("serviceLevel", 0);
			editor.commit();
			statusText.setTextColor(Color.rgb(100, 0, 0));
			statusText.setText("OFF");
			commentText.setTextColor(Color.rgb(100, 100, 100));
			commentText.setText("Slide the bar right to turn the service on & set the desired level of activity...");
		}
		else {
			seekBar.setProgress(sharedPreferences.getInt("serviceLevel", 0));
			seekBar.setSecondaryProgress(sharedPreferences.getInt("serviceLevel", 0));
			if(sharedPreferences.getInt("serviceLevel", 0) > 0) {
				statusText.setTextColor(Color.rgb(0, 100, 0));
				statusText.setText("ON");
				commentText.setTextColor(Color.rgb(255, 255, 255));
				commentText.setText("The service will download tasks from the server, execute them & send the results back.");
				if(!isServiceRunning()) {
					Log.i(TAG, "onStart: Restarting...");
					startService(new Intent(ClientActivity.this, FileService.class));
				}
			}
			else {
				statusText.setTextColor(Color.rgb(100, 0, 0));
				statusText.setText("OFF");
				commentText.setTextColor(Color.rgb(100, 100, 100));
				commentText.setText("Nothing to do.");
			}
		}
	}
	@Override
	protected void onStart() {
		super.onStart();
		if(restartService && isServiceRunning()) {
			Log.i(TAG, "onStart: Restarting...");
			restartService = false;
			stopService(new Intent(ClientActivity.this, FileService.class));
			startService(new Intent(ClientActivity.this, FileService.class));
		}
	}
	private OnClickListener okListener = new OnClickListener() {
		public void onClick(View v){
			if(seekBar.getProgress() > 0 && sharedPreferences.getInt("serviceLevel", 0) == 0) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putInt("serviceLevel", seekBar.getProgress());
				editor.commit();
				seekBar.setSecondaryProgress(seekBar.getProgress());
				startService(new Intent(ClientActivity.this, FileService.class));
			}
			else if(seekBar.getProgress() == 0) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putInt("serviceLevel", seekBar.getProgress());
				editor.commit();
				seekBar.setSecondaryProgress(seekBar.getProgress());
				stopService(new Intent(ClientActivity.this, FileService.class));
			}
			else {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putInt("serviceLevel", seekBar.getProgress());
				editor.commit();
				seekBar.setSecondaryProgress(seekBar.getProgress());
			}
		}
	};
	private OnClickListener cancelListener = new OnClickListener() {
		public void onClick(View v){
			seekBar.setProgress(sharedPreferences.getInt("serviceLevel", 0));
			seekBar.setSecondaryProgress(sharedPreferences.getInt("serviceLevel", 0));
			levelText.setText(null);
		}
	};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_client, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
			startActivity(settingsActivity);
			return true;
		default:return super.onOptionsItemSelected(item);
		}
	}
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		int c = (int)(100 + arg0.getProgress()*1.55);
		levelText.setTextColor(Color.rgb(c, c, c));
		levelText.setText(arg0.getProgress() + "%");
		if(arg1 == 0) {
			statusText.setTextColor(Color.rgb(100, 0, 0));
			statusText.setText("OFF");
			commentText.setTextColor(Color.rgb(100, 100, 100));
			commentText.setText("Nothing to do.");
		}
		else {
			statusText.setTextColor(Color.rgb(0, 100, 0));
			statusText.setText("ON");
			commentText.setTextColor(Color.rgb(255, 255, 255));
			commentText.setText("The service will download tasks from the server, execute them & send the results back.");
		}
	}
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		int c = (int)(100 + arg0.getProgress()*1.55);
		levelText.setTextColor(Color.rgb(c, c, c));
		levelText.setText(arg0.getProgress() + "%");
	}
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		levelText.setText(null);
	}
	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (FileService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
