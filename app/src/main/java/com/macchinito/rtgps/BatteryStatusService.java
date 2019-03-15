package com.macchinito.rtgps;

import android.app.Service; 
import android.content.Context;
import android.content.Intent; 
import android.content.IntentFilter;
import android.os.IBinder; 

import android.util.Log;

import android.content.BroadcastReceiver;
import android.os.BatteryManager;

import static com.macchinito.rtgps.CommonUtilities.TAG;

class BatteryStatus {
    static String status;
    static String health;
    static boolean present;
    static int level;
    static int scale;
    static int icon_small;
    static String plugged;
    static int voltage;
    static int temperature;
    static String technology;
}

public class BatteryStatusService extends Service 
{
    static boolean running = false;
    

    @Override
    public void onCreate() 
    {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) 
    {
	if (running) {
	    Log.v(TAG, "BatteryStateService already running");
	    return START_STICKY;
	}

	running = true;
	
	Log.v(TAG, "BatteryStateService:onStart()");

	IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);

	return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) 
    {
        return null;
    }

    @Override
    public void onDestroy() {
	running = false;
        super.onDestroy();
    }   

    // Battery State Receiver
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
		    int status = intent.getIntExtra("status", 0);
		    int health = intent.getIntExtra("health", 0);
		    boolean present = intent.getBooleanExtra("present", false);
		    int level = intent.getIntExtra("level", 0);
		    int scale = intent.getIntExtra("scale", 0);
		    int icon_small = intent.getIntExtra("icon-small", 0);
		    int plugged = intent.getIntExtra("plugged", 0);
		    int voltage = intent.getIntExtra("voltage", 0);
		    int temperature = intent.getIntExtra("temperature", 0);
		    String technology = intent.getStringExtra("technology");
		    
		    String statusString = "";
                
		    switch (status) {
		    case BatteryManager.BATTERY_STATUS_UNKNOWN:
			statusString = "unknown"; break;
		    case BatteryManager.BATTERY_STATUS_CHARGING:
			statusString = "charging"; break;
		    case BatteryManager.BATTERY_STATUS_DISCHARGING:
			statusString = "discharging"; break;
		    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
			statusString = "not charging"; break;
		    case BatteryManager.BATTERY_STATUS_FULL:
			statusString = "full"; break;
		    }
                
		    String healthString = "";
		    switch (health) {
		    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
			healthString = "unknown"; break;
		    case BatteryManager.BATTERY_HEALTH_GOOD:
			healthString = "good"; break;
		    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
			healthString = "overheat"; break;
		    case BatteryManager.BATTERY_HEALTH_DEAD:
			healthString = "dead"; break;
		    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
			healthString = "voltage"; break;
		    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
			healthString = "unspecified failure"; break;
		    }
		    
		    String acString = "";
		    switch (plugged) {
		    case BatteryManager.BATTERY_PLUGGED_AC:
			acString = "plugged ac"; break;
		    case BatteryManager.BATTERY_PLUGGED_USB:
			acString = "plugged usb"; break;
		    }
		    /*
		    Log.v(TAG + " status", statusString);
		    Log.v(TAG + " health", healthString);
		    Log.v(TAG + " present", String.valueOf(present));
		    Log.v(TAG + " level", String.valueOf(level));
		    Log.v(TAG + " scale", String.valueOf(scale));
		    Log.v(TAG + " icon_small", String.valueOf(icon_small));
		    Log.v(TAG + " plugged", acString);
		    Log.v(TAG + " voltage", String.valueOf(voltage));
		    Log.v(TAG + " temperature", String.valueOf(temperature));
		    Log.v(TAG + " technology", technology);
		    */
		    //  Log.v(TAG, "Battery status changed");

		    BatteryStatus.status = statusString;
		    BatteryStatus.health = healthString;
		    BatteryStatus.present = present;
		    BatteryStatus.level = level;
		    BatteryStatus.scale = scale;
		    BatteryStatus.icon_small = icon_small;
		    BatteryStatus.plugged = acString;
		    BatteryStatus.voltage = voltage;
		    BatteryStatus.temperature = temperature;
		    BatteryStatus.technology = technology;

		}
	    }
	};
}
