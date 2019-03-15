/*
 * Copyright TechBooster/mhidaka, kei_i_t
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.macchinito.rtgps;

import java.io.IOException;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import static com.macchinito.rtgps.CommonUtilities.TAG;

public class GetRegIDActivity extends Activity {
    private GoogleCloudMessaging gcm;
    private Context context;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	//        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        
        // GCMクラスを作成して非同期でregist処理を行う。
        gcm = GoogleCloudMessaging.getInstance(this);
        registerInBackground();
    }
 
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    String regid = gcm.register("425538225911");
                    msg = regid;
                    Log.d(TAG, "Device registered, registration ID=" + msg);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
 
            @Override
            protected void onPostExecute(String msg) {
                // registration IDを取得
                // 従来であれば、ここから送信サーバーへregistration IDを送信するような流れになる
                Log.d(TAG, msg); 
            }
        }.execute(null, null, null);
    }
}
