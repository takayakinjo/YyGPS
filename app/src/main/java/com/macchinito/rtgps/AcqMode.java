package com.macchinito.rtgps;

import android.util.Log;

import static com.macchinito.rtgps.CommonUtilities.TAG;

public class AcqMode {

    static int stableServiceCounter = 0; // counter for stable period

    public static void set( boolean contMode ) {

	if (contMode) {
	    Log.v(TAG, "set CONTINUOUS mode");
	    locInfo.contMode = true;
	    stableServiceCounter = 0;

	} else {
	    Log.v(TAG, "set SAMPLING mode");
	    locInfo.contMode = false;

	}

    }

    public static boolean get() {
	return locInfo.contMode;
    }
    
}
