package com.macchinito.rtgps;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.widget.Toast;

import android.util.Log;

// defines
import static com.macchinito.rtgps.CommonUtilities.TAG;

public class asyncHttp extends AsyncTask<Uri.Builder, Void, String> {

    String result = null;
    
    @SuppressLint("UnlocalizedSms") @Override
	protected String doInBackground(Builder... params) {
	try {
	    // send mail
	    sendMail ms = new sendMail();

	    for ( int i = 0; i < 10 ; i ++) {
		result = null;
		Log.v(TAG, "sending mail(" + String.valueOf(i+1) + ")");
		if (ms.send()) break;
		
		try { // wait 5 sec
		    Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		result = "gave up";
	    }
	    
	} catch (Exception e) {
	    return e.toString();
	}
	return result;
    }

    @Override
	protected void onPostExecute(String result) {
	if (result != null)
	    Log.v(TAG, "Mail sent failed(" + result + ")");
	else
	    Log.v(TAG, "Mail sent completed" );
    }
}
