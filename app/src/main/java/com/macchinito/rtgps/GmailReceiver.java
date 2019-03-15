package com.macchinito.rtgps;

import android.util.Log;
import android.content.*;

import static com.macchinito.rtgps.CommonUtilities.TAG;

public class GmailReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.v(TAG, "Get location request received(getLoc)");

	mailInfo.To = "whereisryuto@macchinito.com";
	mailInfo.To2 = null;
	mailInfo.locInfo = locInfo.newInfo;
	mailInfo.logString = "Ping-by-mail response";
	mailInfo.ryutoMessage = null;
	mailInfo.counter = locInfo.counter;
	mailInfo.reason = locInfo.reason;
		    
	// intent for SendMailService
	Intent i = new Intent(context, SendMailService.class);
	context.startService(i);
	Log.v(TAG, "Ping-by-mail response");

	/*
	// start location service any case, for safe
	Intent iLoc = new Intent(context, LocationService.class);
	context.startService(iLoc);
	*/
    }

}
