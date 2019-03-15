package com.macchinito.rtgps;

// from new environment!!!

import android.annotation.SuppressLint;
import android.app.Service; 
import android.content.Context;
import android.content.Intent; 
import android.os.IBinder; 

import android.os.Bundle;
import android.util.Log;

import android.os.Handler;

// location service
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

// log file write
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

// date, calendar
import java.util.Date; 
import java.text.SimpleDateFormat; 

// tones
import android.media.AudioManager; 
import android.media.ToneGenerator;

// defines
import static com.macchinito.rtgps.CommonUtilities.TAG;
import static com.macchinito.rtgps.CommonUtilities.MAIL1;
import static com.macchinito.rtgps.CommonUtilities.MAIL2;
import static com.macchinito.rtgps.CommonUtilities.SERVER_MAIL;

class locInfo {
    static double latitude, longitude;
    static float accuracy;
    static double altitude;
    static long time;
    static float speed;
    static float bearing;
    static String provider = "N/A since service boot up";

    static boolean requested = false; // requested by command flag
    static boolean updated = false; // at least one location info detected
    static String reason; // detected mode
    static long counter = 0; // acq counter
    static boolean contMode = false; // continuous mode flag
    // static boolean contMode = true; // continuous mode flag
    static String info; // location info, append upon new detection
    static String newInfo; // latest location info
    static boolean atHomeMail = false;
    
    static boolean running = false;
}

class prevLocInfo { // previous info for movement check
    static double latitude, longitude;
    static long time;
}

class prevLocInfo2 { // previous info for speed calculation
    static double latitude, longitude;
    static long time;
}

public class LocationService extends Service 
{
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;

    public static final int TIMEOUT1 = 90000; // gps search timeout

    public static final int MAX_LOCATION_NUM = 1; // maximum number for location

    public static final double DISTANCE_MOVED = 50.0; // minimum for distance move
    public static final int STABLE_SERVICE_NUM = 3; // stable counter threshold

    public static final double ALARM_DISTANCE = 500.0; // distance to target
    public static final double ENABLE_DISTANCE = 3000.0; // distance to enable target alarm

    public static final double HOME_NOTICE_DISTANCE = 50.0; // distance to home
    public static final double SCHOOL_NOTICE_DISTANCE = 50.0; // distance to school
    public static final double NOTICE_ENABLE_DISTANCE = 1000.0; // distance to enable notice

    // public static final double TARGET_LAT = 35.573304; // food one lat
    // public static final double TARGET_LNG = 139.394082; // food one lng
    public static final double TARGET_LAT = 35.625225; // bus stop lat
    public static final double TARGET_LNG = 139.655594; // bus stop lng
    public static final double PREV_BUSSTOP_LAT = 35.627761; // prev bus stop lat
    public static final double PREV_BUSSTOP_LNG = 139.656149; // prev bus stop lng
    public static final double HOME_LAT = 35.625492; // home lat
    public static final double HOME_LNG = 139.652385; // home lng
    public static final double SCHOOL_LAT = 35.713185; // school lat
    public static final double SCHOOL_LNG = 139.575287; // school lng

    static double distance;
    static long currentTimeMillis;
    static int terminateMode = 0; // terminate mode
    static boolean serviceRunning = false; // server running flag
        
    // static int stableServiceCounter = 0; // counter for stable period
    
    // notice enable flag, enabled when being far from target
    static boolean alarmEnable = false;
    static boolean homeNoticeEnable = false;
    static boolean schoolNoticeEnable = false;

    static int locCount;

    private static boolean contModeFlag = false;

    private final Handler handler = new Handler();

    public void writeLog(String writeString) {
	
	// Log.v(TAG, "wrote log:" + writeString);
	// Write
	try {
	    FileOutputStream fileOutputStream = openFileOutput("log.txt", MODE_APPEND|MODE_WORLD_READABLE);
	    fileOutputStream.write(writeString.getBytes());
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}
    }

    Runnable first_timeout = new Runnable(){
	    @Override
	    public void run() {
		Log.v(TAG, "detection timeout");

		terminateMode = 2;
		stopSelf(); // stop LocationService
	    }
	};
    
    public void callTone() {
	ToneGenerator toneGenerator = 
	    new ToneGenerator(AudioManager.STREAM_SYSTEM,
			      ToneGenerator.MAX_VOLUME);
	//		    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
	toneGenerator.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING);
	
	try {
	    Thread.sleep(2000);
	} catch (InterruptedException e) {
	}
	toneGenerator.stopTone();
	toneGenerator.release();
    }
    
    public void terminateService(int mode) {
	String reason = "";
	// stop all services
	locationManager.removeUpdates(listener);
	
	switch(mode) {
	case 1: reason = "detected"; break;
	case 2: reason = "timeout"; break;
	case 3: reason = "next service"; break;
	default: reason = "undefined"; break;
	}

	Log.v(TAG, "----- location acq stopped(" + reason + ")--");

	locInfo.reason = reason;

	Date date = new Date(currentTimeMillis);
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
	
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
	SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
	Date nowTime = new Date(locInfo.time);
	//  sdf.format(nowTime)

	// start of log
	writeLog("[" + String.valueOf(locInfo.counter) + "] " +
		 simpleDateFormat.format(date) +
		 " Batt:" + String.valueOf(BatteryStatus.level) + 
		 " Mode:" + reason +
		 " ");

	//if (mode == 3 || mode == 4 || locInfo.requested || ((locInfo.counter % 12) == 0) || locInfo.contMode || locInfo.atHomeMail) { // sendmail request
	if (true) { // sendmail request always

	    writeLog("Mail");
	    
	    if (locInfo.requested) {
		writeLog("(req)");
		mailInfo.logString = "Requested message";
	    } else if (locInfo.contMode) {
		writeLog("(cnt)");
		mailInfo.logString = "Continuous Mode message";
	    } else  {
		writeLog("(hbt)");
		mailInfo.logString = "Heart Beat message";
	    }

	    // invoke SendMailService
	    if (locInfo.atHomeMail) {
		// set atHomeMail params
		mailInfo.To = SERVER_MAIL;
		mailInfo.To2 = MAIL2;
		mailInfo.locInfo = locInfo.info;
		mailInfo.logString = "@home notice";
		mailInfo.ryutoMessage = "帰宅しました";
		mailInfo.counter = locInfo.counter;
		mailInfo.reason = locInfo.reason;
		
		locInfo.atHomeMail = false; // reset flag

	    } else {
		mailInfo.To = SERVER_MAIL;
		mailInfo.To2 = null;
		mailInfo.locInfo = locInfo.info;
		mailInfo.ryutoMessage = null;
		mailInfo.counter = locInfo.counter;
		mailInfo.reason = locInfo.reason;
	    }
	    Context context = getApplicationContext();
	    Intent imm = new Intent(context, SendMailService.class);
	    context.startService(imm);

	}
	locInfo.requested = false; // release flag

	// write return code
	writeLog("\n");
	// write location info if any
	if (locInfo.info != null)
	    writeLog(locInfo.info);

	// check sample mode
	Log.v(TAG, "Stable service count:" + String.valueOf(AcqMode.stableServiceCounter) );
	if (!contModeFlag) {
	    AcqMode.stableServiceCounter ++;
	    if (AcqMode.stableServiceCounter >= STABLE_SERVICE_NUM) {
		AcqMode.set(false); // set sampling mode
		AcqMode.stableServiceCounter = 0; // reset stable counter
	    }
	} else {
	    AcqMode.stableServiceCounter = 0; // reset stable counter
	}
	
	serviceRunning = false;

    }

    @Override
    public void onCreate() 
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) 
    {
	// TESTCODE prevent duplicated service
	if (locInfo.running) {
	    Log.v(TAG, "calcelled since already running");
	    return START_NOT_STICKY;
	} else locInfo.running = true;
	
	Log.v(TAG, "LocationService() started");
	
	if (serviceRunning) { // terminate if previous service is running, usually happens on continuous mode
	    Log.v(TAG, "LocationService terminated by next service");
	    terminateService(3);
	}

	serviceRunning = true; // flag for process running
	contModeFlag = false; // flag for continuous mode
	locInfo.counter ++; // inclement counter
	locInfo.info = ""; // reset info data
	locCount = 0; // counter for this acquisition
	currentTimeMillis = System.currentTimeMillis(); // set timestamp

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	if (listener == null)
	    listener = new MyLocationListener();

	locInfo.updated = false; // reset update flag

	// check location provider availability
	final boolean netEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	if (netEnabled)
	    Log.v(TAG, "network provider enabled");
	else
	    Log.v(TAG, "network provider DISBLED");
	if (gpsEnabled)
	    Log.v(TAG, "GPS provider enabled");
	else
	    Log.v(TAG, "GPS provider DISBLED");

	// register locationManagerS
	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
					       4000, 0, listener);
	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
					       4000, 0, listener);

	Log.v(TAG, "[" + String.valueOf(locInfo.counter) + "]+++++ location acq started +++++");

	handler.postDelayed( first_timeout, TIMEOUT1 ); // set timeout

	return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) 
    {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
	    // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
						    currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
	    return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Log.v(TAG, "LocationService::onDestroy");
	locInfo.running = false;
	terminateService(terminateMode);
    }   

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {
	    if(isBetterLocation(loc, previousBestLocation)) {

		loc.getLatitude();
		loc.getLongitude();
		
		if (loc.getAccuracy() > 500.0) // some strange reply, could be from 3G
		    return;

		// update info
		locInfo.latitude = loc.getLatitude();
		locInfo.longitude = loc.getLongitude();
		locInfo.accuracy = loc.getAccuracy();
		locInfo.altitude = loc.getAltitude();
		locInfo.time = loc.getTime();
		locInfo.speed = loc.getSpeed();
		// locInfo.bearing = loc.getBearing();
		locInfo.provider = loc.getProvider();
		locInfo.updated = true;

		locCount ++;

		// 
		float dist2 = (float)CalcDistance.calcDistHubeny(locInfo.latitude, locInfo.longitude,
								 prevLocInfo2.latitude, prevLocInfo2.longitude );
		float timeDiff = ((float)locInfo.time - (float)prevLocInfo2.time)/1000.0F;
		
		if (timeDiff > 0.0F)
		    locInfo.bearing = dist2/timeDiff;
		else
		    locInfo.bearing = 0.0F;

		prevLocInfo2.latitude = locInfo.latitude;
		prevLocInfo2.longitude = locInfo.longitude;
		prevLocInfo2.time = locInfo.time;
		//
		locInfo.newInfo = 
		    "Loc:" + 
		    String.valueOf(locInfo.latitude) + ", " + 
		    String.valueOf(locInfo.longitude) + ", " +
		    String.valueOf(locInfo.time) + ", " +
		    String.valueOf(locInfo.accuracy) + ", " +
		    String.valueOf(locInfo.altitude) + ", " +
		    String.valueOf(locInfo.speed) + ", " +
		    String.valueOf(locInfo.bearing) + ", " +
		    locInfo.provider + "\n";

		distance = CalcDistance.calcDistHubeny(locInfo.latitude, locInfo.longitude,
						       prevLocInfo.latitude, prevLocInfo.longitude );

		Log.v(TAG, "(" + String.valueOf(locInfo.latitude) + ", " + String.valueOf(locInfo.longitude) + ")" + 
		      "@" + String.valueOf(locInfo.time) + " d=" + distance);

		// distance check for cont/sample mode
		if (distance > DISTANCE_MOVED) {
		    Log.v(TAG, "moved more than " + String.valueOf(DISTANCE_MOVED) + "m!");
		    prevLocInfo.latitude = locInfo.latitude;
		    prevLocInfo.longitude = locInfo.longitude;

		    AcqMode.set(true); // set continue mode

		    contModeFlag = true; // default is false

		} else {}

		// distance check for home arrival
		double distToHome = CalcDistance.calcDistHubeny(locInfo.latitude, locInfo.longitude,
								HOME_LAT, HOME_LNG );
		if (distToHome < NOTICE_ENABLE_DISTANCE) {
		    if (distToHome < HOME_NOTICE_DISTANCE) {
			Log.v(TAG, "@HOME");
			if (homeNoticeEnable) { // arrived @home
			    callTone();
			    locInfo.atHomeMail = true;
			    writeLog("-- @HOME --\n");
			    Log.v(TAG, "-- @HOME --");
			    homeNoticeEnable = false; // prevent duplicate notice
			}
		    }
		} else {
		    homeNoticeEnable = true; // enable again once outside
		}

		locInfo.info = locInfo.newInfo + locInfo.info; // append anyway
		
		if (locCount >= MAX_LOCATION_NUM) { // already enough acquired

		    handler.removeCallbacks(first_timeout);

		    terminateMode = 1;
		    stopSelf(); // stop LocationService
		}

            }
        }
	
        public void onProviderDisabled(String provider)
        {
	    Log.v(TAG, "Provider disabled");
        }
	
        public void onProviderEnabled(String provider)
        {
	    Log.v(TAG, "Provider enabled");
        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {
	    Log.v(TAG, "Provider status changed");
        }
    }
}
