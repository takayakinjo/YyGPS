package com.macchinito.rtgps;

import java.io.IOException;
import android.widget.CalendarView;

//GCM 
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.content.Context;
import android.content.Intent;

import android.widget.Toast;

import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.pm.PackageManager;

// button
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.macchinito.rtgps.R.id;

// camera
import android.provider.MediaStore;

import android.os.AsyncTask;

import java.util.concurrent.atomic.AtomicInteger;

import android.view.Window;

import android.view.Menu;
import android.view.MenuItem;
import android.net.Uri;

import android.hardware.Camera;

import android.os.Handler;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import android.widget.TextView;

import static com.macchinito.rtgps.CommonUtilities.SENDER_ID;
import static com.macchinito.rtgps.CommonUtilities.TAG;
import static com.macchinito.rtgps.CommonUtilities.MAIL1;
import static com.macchinito.rtgps.CommonUtilities.MAIL2;

public class MainActivity extends Activity {

	private GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();

	private Context context;

	public static final int SCAN_INTERVAL = 300000; // 5 minutes
	//public static final int SCAN_INTERVAL = 60000; // 1 min. test

	private Camera camera = null;
	private static boolean ledOn = false;

	CalendarView cal;

	private Handler mHandler;
	private Timer mTimer;
	//private static SimpleDateFormat mSimpleDataFormat = new SimpleDateFormat("yyyy年　MM月dd日　HH:mm:ss");
	private static SimpleDateFormat mSimpleDataFormat = new SimpleDateFormat("h:mm:ss");
	private static SimpleDateFormat mSimpleDataFormat2 = new SimpleDateFormat("M月d日E曜日");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		context = getApplicationContext();
		gcm = GoogleCloudMessaging.getInstance(this);

		cal = (CalendarView) findViewById(R.id.calendarView1);
		cal.setShowWeekNumber(false);
		//cal.setTodayHighlighted = true;

		mHandler = new Handler(getMainLooper());
		mTimer = new Timer();

		// runs every one sec
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					public void run() {
						Calendar calendar = Calendar.getInstance();
						String nowDate = mSimpleDataFormat.format(calendar.getTime());
						String nowDate2 = mSimpleDataFormat2.format(calendar.getTime());
						// textView to show time
						((TextView) findViewById(R.id.clock)).setText(nowDate);
						((TextView) findViewById(R.id.clock2)).setText(nowDate2);
					}
				});}
		},0,1000);

		ImageButton buttonSMS1 = (ImageButton)findViewById(id.sms1);
		buttonSMS1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int with = 0;
				// jump to another package
		    /*
		    PackageManager packageManager = getPackageManager();
		    Intent intentActivity = packageManager.getLaunchIntentForPackage("com.macchinito.rtsms");
		    intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    intentActivity.putExtra("with", with);
		    context.startActivity(intentActivity);
		    */

				Intent smsIntent = new Intent(Intent.ACTION_VIEW);
				smsIntent.setType("vnd.android-dir/mms-sms");
				smsIntent.putExtra("address", "09041781684");
				// smsIntent.putExtra("sms_body","Body of Message");
				startActivity(smsIntent);

/*
				Intent intentActivity = new Intent();
				intentActivity.setAction(Intent.ACTION_VIEW);
				intentActivity.setClassName("com.macchinito.rtsms", "com.macchinito.rtsms.BalloonMessageActivity");
				intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intentActivity.putExtra("with", with);
				//startActivity(intentActivity);
				try {
					startActivity(intentActivity);
				} catch (Exception e) {
					Toast.makeText(context, "対象のアプリがありません", Toast.LENGTH_SHORT).show();
				}
*/
			}
		});

		ImageButton buttonSMS2 = (ImageButton)findViewById(id.sms2);
		buttonSMS2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent smsIntent = new Intent(Intent.ACTION_VIEW);
				smsIntent.setType("vnd.android-dir/mms-sms");
				smsIntent.putExtra("address", "09018083349");
				startActivity(smsIntent);
				/*
				int with = 1;
				Intent intentActivity = new Intent();
				intentActivity.setAction(Intent.ACTION_VIEW);
				intentActivity.setClassName("com.macchinito.rtsms", "com.macchinito.rtsms.BalloonMessageActivity");
				intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intentActivity.putExtra("with", with);
				//startActivity(intentActivity);
				try {
					startActivity(intentActivity);
				} catch (Exception e) {
					Toast.makeText(context, "対象のアプリがありません", Toast.LENGTH_SHORT).show();
				}
				*/
			}
		});


		ImageButton buttonSkype = (ImageButton)findViewById(id.skype);
		buttonSkype.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// jump to another package
				PackageManager packageManager = getPackageManager();

				try {
					Intent intentActivity = packageManager.getLaunchIntentForPackage("com.skype.raider");
					intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					context.startActivity(intentActivity);
				} catch (Exception e) {
					Toast.makeText(context, "対象のアプリがありません", Toast.LENGTH_SHORT).show();
				}
			}
		});


		ImageButton led_button = (ImageButton) findViewById(id.led_button);
		led_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!ledOn) {
					ledOnOff(true);
				} else {
					ledOnOff(false);
				}
			}
		});

		// start BatteryStateService // moved to getLoc intent receiver
		// Context c = getBaseContext();
		// Intent iBat = new Intent(c, BatteryStatusService.class);
		// context.startService(iBat);

	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(id.led_button).setActivated(false);

		// start alarm scheduler, every 5 min // moved to bootup starter but just to make sure
		RegisterRoutine registerRoutine = new RegisterRoutine(context);
		registerRoutine.register();

		// set today's date
		cal.setDate(System.currentTimeMillis());
		// scheduleService();
	}

	@Override
	protected void onPause() {
		super.onPause();

		ledOnOff(false);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// stop timer
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	private void ledOnOff(boolean on) {

		findViewById(id.led_button).setActivated(on);

		if (on) {
			camera = Camera.open();
			camera.startPreview();
			Camera.Parameters params = camera.getParameters();
			params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			camera.setParameters(params);
			ledOn = true;
		} else {
			if (camera != null) {
				camera.stopPreview();
				camera.release();
				camera = null;
				ledOn = false;
			}
		}
	}

	protected void scheduleService(){
		Context context = getBaseContext();
		Intent intent = new Intent(context, RoutineService.class);
		PendingIntent pendingIntent
				= PendingIntent.getService(
				context, -1, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager
				= (AlarmManager)
				context.getSystemService(ALARM_SERVICE);
		alarmManager.setInexactRepeating(// AlarmManager.RTC,
				AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(),
				SCAN_INTERVAL, pendingIntent);
	}

	protected void cancelService(){
		Context context = getBaseContext();
		Intent intent = new Intent(context, LocationService.class);
		PendingIntent pendingIntent
				= PendingIntent.getService(
				context, -1, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager
				= (AlarmManager)
				context.getSystemService(ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}

	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					String regid = gcm.register(SENDER_ID);
					msg = regid;
					Log.d(TAG, "Device registered, registration ID=" + msg);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				// get registration ID
				// send registration ID to server
				Log.d(TAG, msg);
			}
		}.execute(null, null, null);
	}

	// otions menu creation
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);

		return true;
	}

	// menu select event
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent iBat = new Intent(context, BatteryStatusService.class);

		switch (item.getItemId()) {
			case R.id.menu1: // SETTINGS
				// invoke settings
				//Intent intent =  new Intent()
				//.setAction(android.provider.Settings.ACTION_SETTINGS)
				//.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				//startActivity(intent);
				// to password inputs
				Intent intentp = new Intent(context, passenterActivity.class);
				startActivity(intentp);
				break;
			case R.id.menu2: // REGISTER GCM
				// register device for GCM
				registerInBackground();
				break;

			case R.id.menu3: // START GPS SERVICE
				// start BatteryStateService
				context.startService(iBat);

				// start location service any case, for safe
				AcqMode.set(true); // continuous mode
				Intent iLoc = new Intent(context, LocationService.class);
				context.startService(iLoc);
				break;

			case R.id.menu4: // SEND KEEPALIVE
				// send keepalive for test
				GcmKeepAlive gcmKeepAlive = new GcmKeepAlive(this);
				gcmKeepAlive.broadcastIntents();
				break;

			case R.id.menu5: // START FUSED LOCATION SERVICE
				// start BatteryStateService
				context.startService(iBat);

				// start fused location service
				Intent iBLoc = new Intent(context, FuseLocationService.class);
				context.startService(iBLoc);
				break;

			case R.id.menu6: // REGISTER ROUTINE
				// register routine service
				RegisterRoutine registerRoutine = new RegisterRoutine(context);
				registerRoutine.register();

				break;

		}
		return super.onOptionsItemSelected(item);
	}
}

class sendTest {

	public static String sendMessage(GoogleCloudMessaging gcm, AtomicInteger msgId) {

		String msg = "";
		try {
			Bundle data = new Bundle();
			data.putString("my_message", "Hello World");
			data.putString("my_action",
					"com.google.android.gcm.demo.app.ECHO_NOW");
			String id = Integer.toString(msgId.incrementAndGet());
			gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
			msg = "Sent message";
		} catch (IOException ex) {
			msg = "Error :" + ex.getMessage();
		}

		Log.v(TAG, "sendtest:" + msg);
		return msg;
	}
}
