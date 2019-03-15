package com.macchinito.rtgps;
 
import android.os.Bundle;
import android.app.Activity;
import android.view.Window;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.content.Intent;
 
public class WebViewActivity extends Activity {
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.webview);
         
        WebView  myWebView = (WebView)findViewById(R.id.webView1);
	WebSettings webSettings = myWebView.getSettings();
	webSettings.setJavaScriptEnabled(true);

        myWebView.setWebViewClient(new WebViewClient());

	Intent i = getIntent();
	String url = i.getStringExtra("URL");
         
        myWebView.loadUrl(url);
    }
    
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    */
}
