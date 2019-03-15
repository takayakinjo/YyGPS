package com.macchinito.rtgps;

import android.os.Bundle;
import android.util.Log;

import android.content.*;
import android.app.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;

import static com.macchinito.rtgps.CommonUtilities.TAG;

// called on boot up
public class StartupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Boot Startup received:");

	RegisterRoutine registerRoutine = new RegisterRoutine(context);
	registerRoutine.register();

    }
}
