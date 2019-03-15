package com.macchinito.rtgps;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
//import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Service; 

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GoogleApiAvailability;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import java.util.Date; 
import java.text.SimpleDateFormat; 
import java.util.Calendar;
import java.text.DateFormat;

// defines
import static com.macchinito.rtgps.CommonUtilities.TAG;
import static com.macchinito.rtgps.CommonUtilities.MAIL1;
import static com.macchinito.rtgps.CommonUtilities.MAIL2;
import static com.macchinito.rtgps.CommonUtilities.SERVER_MAIL;

/**
 *
 * FuseLocationService used for tracking user location in the background.
 * @author cblack
 */
public class FuseLocationService extends Service implements
						     GoogleApiClient.ConnectionCallbacks,
						     GoogleApiClient.OnConnectionFailedListener,
						     LocationListener {
    
    IBinder mBinder = new LocalBinder();

    private GoogleApiClient mGoogleApiClient;
    private PowerManager.WakeLock mWakeLock;
    private LocationRequest mLocationRequest;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    private Boolean servicesAvailable = false;

    private static int stableCounter = 0;

    private static boolean running = false;

    private static boolean homeNoticeEnable = false;
    
    public class LocalBinder extends Binder {
        public FuseLocationService getServerInstance() {
            return FuseLocationService.this;
        }
    }

    static int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        mInProgress = false;
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);

        servicesAvailable = servicesConnected();

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        setUpLocationClientIfNeeded();

    }

    /*
     * Create a new location client, using the enclosing class to
     * handle callbacks.
     */
    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
    }

    private boolean servicesConnected() {

        // Check that Google Play services is available
	int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	//int resultCode = GoogleApiAvailability.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
	    Log.v(TAG, "Play Service AVAILABLE");
            return true;
        } else {
	    Log.v(TAG, "Play Service UNAVAILABLE");

            return false;
        }
	
    }

    public int onStartCommand (Intent intent, int flags, int startId)
    {
	stableCounter = 0; // reset stable counter

	if (running) {
	    Log.v(TAG, "Already running. ignored");
	    return START_STICKY;
	    //stopService(); // stop first if already running
	}
	running = true;

	Log.v(TAG, "onStartCommand(B)");

        super.onStartCommand(intent, flags, startId);

        PowerManager mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);

        /*
        WakeLock is reference counted so we don't want to create multiple WakeLocks. So do a check before initializing and acquiring.

        This will fix the "java.lang.Exception: WakeLock finalized while still held: MyWakeLock" error that you may find.
        */
        if (this.mWakeLock == null) { //**Added this
            this.mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        }

        if (!this.mWakeLock.isHeld()) { //**Added this
            this.mWakeLock.acquire();
        }

        if(!servicesAvailable || mGoogleApiClient.isConnected() || mInProgress)
            return START_STICKY;

        setUpLocationClientIfNeeded();
        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !mInProgress)
        {
            appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Started", Constants.LOG_FILE);
            mInProgress = true;
            mGoogleApiClient.connect();
        }


        return START_STICKY;
    }


    private void setUpLocationClientIfNeeded()
    {
        if(mGoogleApiClient == null)
            buildGoogleApiClient();
    }


    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ":" + msg, Constants.LOCATION_FILE);


	//
	locInfo.latitude = location.getLatitude();
	locInfo.longitude = location.getLongitude();
	locInfo.time = location.getTime();
	double distance = CalcDistance.calcDistHubeny(locInfo.latitude, locInfo.longitude,
						      prevLocInfo.latitude, prevLocInfo.longitude );

	Log.v(TAG, "(" + String.valueOf(locInfo.latitude) + ", " + String.valueOf(locInfo.longitude) + ")" + 
	      "@" + String.valueOf(locInfo.time) + " d=" + distance);

	// distance check for cont/sample mode
	if (distance > Constants.DISTANCE_MOVED) {
	    Log.v(TAG, "moved more than " + String.valueOf(Constants.DISTANCE_MOVED) + "m!");
	    prevLocInfo.latitude = locInfo.latitude;
	    prevLocInfo.longitude = locInfo.longitude;

	    stableCounter = 0;

	} else {
	    stableCounter ++;
	}

	Log.v(TAG, "fused stable Counter:"+stableCounter);
	
	if (stableCounter > Constants.STABLE_SERVICE_NUM) {
	    // stop this service
	    Log.v(TAG, "fusedLocationService stopped");
	    stopSelf();
	}

	// send mail
	mailInfo.To = SERVER_MAIL;
	mailInfo.To2 = null;
	mailInfo.locInfo =
	    "Loc:" + 
	    String.valueOf(location.getLatitude()) + ", " + 
	    String.valueOf(location.getLongitude()) + ", " +
	    String.valueOf(location.getTime()) + ", " +
	    String.valueOf(location.getAccuracy()) + ", " +
	    String.valueOf(location.getAltitude()) + ", " +
	    String.valueOf(location.getSpeed()) + ", " +
	    String.valueOf(location.getBearing()) + ", " +
	    location.getProvider() + "\n";
	
	mailInfo.ryutoMessage = null;
	mailInfo.counter = counter++;
	mailInfo.reason = location.getProvider();
	mailInfo.logString = "Continuous Mode message";
	
	// distance check for home arrival
	double distToHome = CalcDistance.calcDistHubeny(locInfo.latitude, locInfo.longitude,
							Constants.HOME_LAT, Constants.HOME_LNG );
	if (distToHome < Constants.NOTICE_ENABLE_DISTANCE) {
	    if (distToHome < Constants.HOME_NOTICE_DISTANCE) {
		Log.v(TAG, "@HOME");
		if (homeNoticeEnable) { // arrived @home
		    // callTone();
		    // overwrite configuration
		    mailInfo.To2 = MAIL2;
		    mailInfo.ryutoMessage = "帰宅しました";
		    mailInfo.logString = "@home notice";
		    
		    homeNoticeEnable = false; // prevent duplicate notice
		}
	    }
	} else {
	    homeNoticeEnable = true; // enable again once outside
	}

	Context context = getApplicationContext();
	Intent imm = new Intent(context, SendMailService.class);
	context.startService(imm);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public String getTime() {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return mDateFormat.format(new Date());
    }

    public void appendLog(String text, String filename)
    {

	Log.v(TAG, text);

        File logFile = new File(filename);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {

	Log.v(TAG, "onDestroy(B)");

	stopService();

        super.onDestroy();
    }

    // stopService
    private void stopService()
    {
        // Turn off the request flag
        this.mInProgress = false;

        if (this.servicesAvailable && this.mGoogleApiClient != null) {
            this.mGoogleApiClient.unregisterConnectionCallbacks(this);
            this.mGoogleApiClient.unregisterConnectionFailedListener(this);
            this.mGoogleApiClient.disconnect();
            // Destroy the current location client
            this.mGoogleApiClient = null;
        }
        // Display the connection status
        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ":
        // Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();

        if (this.mWakeLock != null) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }

	running = false;
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {

        // Request location updates using static settings
	// TESTCODE
        // Intent intent = new Intent(this, LocationReceiver.class);
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,
                mLocationRequest, this); // This is the changed line.
        appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Connected", Constants.LOG_FILE);

    }

    /*
 * Called by Location Services if the connection to the
 * location client drops because of an error.
 */
    @Override
    public void onConnectionSuspended(int i) {
        // Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        mGoogleApiClient = null;
        // Display the connection status
        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected", Constants.LOG_FILE);
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {

            // If no resolution is available, display an error dialog
        } else {

        }
    }
}
