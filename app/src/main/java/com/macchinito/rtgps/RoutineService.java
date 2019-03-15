package com.macchinito.rtgps;

import android.app.Service; 
import android.content.Context;
import android.content.Intent; 
import android.os.IBinder; 

import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import android.os.AsyncTask;

//GCM 
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.concurrent.atomic.AtomicInteger;

import static com.macchinito.rtgps.CommonUtilities.TAG;
import static com.macchinito.rtgps.CommonUtilities.SENDER_ID;

public class RoutineService extends Service 
{

    private GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    private Context context;

    static int counter = 0;
    
    @Override
    public void onCreate() 
    {
	super.onCreate();

        context = getApplicationContext();
        gcm = GoogleCloudMessaging.getInstance(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) 
    {
	Log.v(TAG, "Routine Service started("+counter+")");
	
	GcmKeepAlive gcmKeepAlive = new GcmKeepAlive(this);
	gcmKeepAlive.broadcastIntents();
	//sendMessageInBackground();

	// locationService
	// if (AcqMode.get() || (counter%12 == 0) ) { // continuous mode and heartbeat every hour
	if (AcqMode.get()) { // continuous mode 
	    Intent iLoc = new Intent(context, LocationService.class);
	    context.startService(iLoc);
	}

	/*
	// start fuse location service // runs continuously for now
	Intent iBLoc = new Intent(context, FuseLocationService.class);
	context.startService(iBLoc);
	*/

	counter ++; // increment counter

	stopSelf(); // stop this service once
	
	return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) 
    {
        return null;
    }

    @Override
    public void onDestroy() {
	Log.v(TAG, "Routine Service completed");
        super.onDestroy();
    }


    private boolean ping(){
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        try {
            proc = runtime.exec("ping -c 5 www.google.com");
            proc.waitFor();
        } catch (Exception e){}
        int exitVal = proc.exitValue();
        if(exitVal == 0)return true;
        else return false;
    }

    private void sendMessageInBackground() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                    data.putString("my_message", "Hello World");
                    data.putString("my_action","SAY_HELLO");
                    String id = Integer.toString(msgId.incrementAndGet());
                    gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.v("TAG", msg);
            }
        }.execute(null, null, null);

    }

}

