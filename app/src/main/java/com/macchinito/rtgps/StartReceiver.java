package com.macchinito.rtgps;

// basics
import android.util.Log;

import android.content.*;

import static com.macchinito.rtgps.CommonUtilities.TAG;

public class StartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.v(TAG, "start locationservice received");

	// start location service
	Intent i = new Intent(context, LocationService.class);
	context.startService(i);

    }

}
