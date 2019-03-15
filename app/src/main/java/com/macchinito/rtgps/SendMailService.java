package com.macchinito.rtgps;

import android.app.Service; 
import android.content.Intent; 
import android.os.IBinder; 

import android.util.Log;

import android.net.Uri;

import java.util.Date; 
import java.text.SimpleDateFormat; 

import static com.macchinito.rtgps.CommonUtilities.TAG;

public class SendMailService extends Service 
{

    @Override
    public void onCreate() 
    {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) 
    {
        Log.v(TAG, "Sendmail service started");

	// send mail reply
	if (mailInfo.To != null) {
	    // send mail
	    if (mailInfo.ryutoMessage == null) {
		mailInfo.Subject = "RtGPS Location Service";
		mailInfo.html = false;
	    } else {
		mailInfo.Subject = mailInfo.ryutoMessage;
		mailInfo.html = true;
	    }

	    String linkString = "http://www.macchinito.com/rtgps/index.php?msg&lat=" + 
		String.valueOf(locInfo.latitude) + "&lng=" + 
		String.valueOf(locInfo.longitude);
	    
	    mailInfo.htmlString = "<html><body>" +
		"RtGPSmessage:<br/>" +
		mailInfo.ryutoMessage + "<br/>" +
		"RtGPSmessage end<p/>" +
		"<a href='"+linkString+"'>"+ "[Reply Message]" + "</a><p/>" +
		"--- <br/> RtGPS Message System <br/>" +
		"</body></html>";

	    SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
	    Date nowTime = new Date(locInfo.time);

	    mailInfo.txtString = 
		mailInfo.locInfo + 
		"\nMessage: " + mailInfo.ryutoMessage +
		"\nLog message: " + mailInfo.logString + ", " + mailInfo.reason + ", count:" + String.valueOf(mailInfo.counter) +
		"\nBattery Information" +
		"\n Status: " + BatteryStatus.status +
		"\n Health: " + BatteryStatus.health +
		"\n Level: " + String.valueOf(BatteryStatus.level) + "%" +
		"\n Temp: " + String.valueOf(BatteryStatus.temperature/10) + "degC\n";

	    /*
	    // sync(lock) version
	    try {
		// send mail
		sendMail ms = new sendMail();
		ms.send();
		
	    } catch (Exception e) {
		Log.v(TAG, e.toString());
	    }
	    Log.v(TAG, "sent mail to "+mailInfo.To + ", " + mailInfo.To2 );
	    */
	    
	    // async version
	    Uri.Builder builder = new Uri.Builder();
	    asyncHttp task = new asyncHttp();
	    task.execute(builder);

	}
	
	// stop this service
	stopSelf(startId);
      
	return START_NOT_STICKY;
    }

    /*
      Toast.makeText(this, 
      "Mail sent to:" + mailInfo.To + ", " + mailInfo.To2 +
      "\nLocation provided by " +
      locInfo.provider +
      "\n(" + mailInfo.logString + ")" +
      "\nBattery " + String.valueOf(BatteryStatus.level) + "%",
      Toast.LENGTH_LONG).show();
    */
    
    @Override
    public IBinder onBind(Intent intent) 
    {
        return null;
    }

    @Override
    public void onDestroy() {       
        super.onDestroy();
    }   

}
