package com.macchinito.rtgps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//import android.support.v7.app.AppCompatActivity;

public class passenterActivity extends Activity {

    static final String PASSWD = "19680304";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.passenter);

        Button returnButton = (Button)findViewById(R.id.pass_ok_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit = (EditText)findViewById(R.id.passwd);
                SpannableStringBuilder sp = (SpannableStringBuilder)edit.getText();
                if (sp.toString().equals(PASSWD)) {
                    Intent intent = new Intent()
                            .setAction(android.provider.Settings.ACTION_SETTINGS)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(intent);
                } else {
                    finish();
                }
            }
        });
    }
}