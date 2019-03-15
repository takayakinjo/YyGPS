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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import static com.macchinito.rtgps.CommonUtilities.TAG;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    public GcmBroadcastReceiver() {
	super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

	Log.v(TAG, "GCM:onReceive()");

	String regId = intent.getExtras().getString("registration_id"); 
 
	if(regId != null && !regId.equals("")) { 

	    Log.v(TAG, "RegID:"+regId );

	    /* Now we can do what ever we want with the regId: 
	     * 1. send it to our server 
	     * 2. store it once successfuly registered on the server side */ 
	}

        // serve intent at GcmIntentService
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        // invoke service, keep WakeLock while working
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);

    }
}
