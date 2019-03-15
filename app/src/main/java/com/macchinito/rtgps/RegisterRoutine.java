package com.macchinito.rtgps;

import android.content.Context;
import android.content.Intent; 
import android.util.Log;

import android.app.AlarmManager;
import android.app.PendingIntent;

import static com.macchinito.rtgps.CommonUtilities.TAG;

public class RegisterRoutine  {

    public static final int SCAN_INTERVAL = 300000; // 5min

    protected Context mContext;

    public RegisterRoutine(Context context) {
	mContext = context;
    }
    
    public void register() {

	Intent iR = new Intent(mContext, RoutineService.class);
	PendingIntent pendingIntent 
	    = PendingIntent.getService(
				       mContext, -1, iR,
				       PendingIntent.FLAG_UPDATE_CURRENT);
	AlarmManager alarmManager 
	    = (AlarmManager)
	    mContext.getSystemService(Context.ALARM_SERVICE);
	alarmManager.setInexactRepeating(// AlarmManager.RTC, 
					 AlarmManager.RTC_WAKEUP, 
					 System.currentTimeMillis(),
					 SCAN_INTERVAL, pendingIntent); // 30sec test
	
	Log.v(TAG, "Scheduled routine registered");
    }
    
}
