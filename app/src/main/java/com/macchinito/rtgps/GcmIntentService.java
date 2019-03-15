/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.macchinito.rtgps;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.media.AudioManager; 
import android.media.ToneGenerator;
import android.os.Handler;
import android.widget.Toast;

import static com.macchinito.rtgps.CommonUtilities.TAG;
import static com.macchinito.rtgps.CommonUtilities.CONTMODETIMEOUT;
import static com.macchinito.rtgps.CommonUtilities.SERVER_MAIL;

public class GcmIntentService extends IntentService {

    private Context mContext;
    private Handler mHandler;

    private final Handler contModeTimeouthandler = new Handler();

    // constructor without parameter needed!!
    public GcmIntentService() {
        super(GcmIntentService.class.getName());
	mHandler = new Handler();
    }
    
    public GcmIntentService(String name) {
        super(name);
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();

	Context context = getBaseContext();
	
        // get GCM message from intent
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        // filter GCM message
        // ignore unknown type
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) { 
                // error
                Log.d(TAG,"messageType: " + messageType + ", send error:" + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                // message deleted at server
                Log.d(TAG,"messageType: " + messageType + ", message deleted:" + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) { 
                // receive message
                Log.d(TAG,"messageType: " + messageType + ", received message:" + extras.toString());

		String message = extras.getString("message");

		if(message.equals("rtgpsCom_getLoc")) { // get new location info

		    // start BatteryStateService
		    Intent iBat = new Intent(context, BatteryStatusService.class);
		    context.startService(iBat);

		    if (! locInfo.requested) { // ignore multiple request
			// request new location acquisition
			locInfo.requested = true;
		    } else {
			Log.v(TAG, "location already requested.");
		    }
		    // start location service any case, for safe
		    AcqMode.set(true); // continuous mode
		    Intent iLoc = new Intent(context, LocationService.class);
		    context.startService(iLoc);

		} else if(message.equals("rtgpsCom_getFuseLoc")) { // get new location info(Fuse mode)

		    // start BatteryStateService
		    Intent iBat = new Intent(context, BatteryStatusService.class);
		    context.startService(iBat);

		    Intent iFLoc = new Intent(context, FuseLocationService.class);
		    context.startService(iFLoc);

		} else if(message.equals("rtgpsCom_ping")) { // quick responce

		    mailInfo.To = SERVER_MAIL;
		    mailInfo.To2 = null;
		    mailInfo.locInfo = locInfo.newInfo;
		    mailInfo.logString = "Ping response";
		    mailInfo.ryutoMessage = null;
		    mailInfo.counter = locInfo.counter;
		    mailInfo.reason = locInfo.reason;
		    
		    // intent for SendMailService
		    Intent i = new Intent(context, SendMailService.class);
		    context.startService(i);
		    Log.v(TAG, "Ping response");

		} else if(message.equals("rtgpsCom_contMode")) { // set continuous mode
		    
		    AcqMode.set(true);

		} else if(message.equals("rtgpsCom_sampleMode")) { // set sampling mode

		    AcqMode.set(false);
		    // stop FuseLocationService
		    Intent iFLoc = new Intent(context, FuseLocationService.class);
		    context.stopService(iFLoc);

		} else if(message.equals("rtgpsCom_call")) {

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

		} else {

		    Log.v(TAG, "message:"+message);

		    /*
		    Intent it = new Intent(getApplicationContext(), MessageActivity.class);
		    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

		    if (message.substring(0, 5).equals("CALL:")) {
			message = message.substring(5, message.length());
			it.putExtra("DIALOG", "ON");
			// TODO: start calling sound
			// TODO: start calling sound
			// TODO: start calling sound
		    }

		    it.putExtra("MESSAGE", message);
		    startActivity(it);
		    */
		    /*
		    mContext = getApplicationContext();
		    mHandler.post(new Runnable(){
			    @Override
			    public void run() {
				Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
			    }
			});
		    */

		}

	    }
        }
        // let Receiver know the completion, free the lock
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

}
