package com.flaremars.classmanagers.mainui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.uis.UserInterfaceGuideActivity;

public class StartUpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        AssetManager assetManager = getAssets();
        Typeface tf = Typeface.createFromAsset(assetManager, "fonts/FZJL.ttf");

        TextView secondTextView = (TextView) findViewById(R.id.second_text_view);
        secondTextView.setTypeface(tf);
        secondTextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = StartUpActivity.this.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,MODE_PRIVATE);
                int startUpCount = sharedPreferences.getInt("start_up_count",0);
                Intent intent;
                if (startUpCount == 0) {
                    intent = new Intent(StartUpActivity.this, UserInterfaceGuideActivity.class);
                    sharedPreferences.edit().putInt("start_up_count",1).apply();
                } else {
                    intent = new Intent(StartUpActivity.this, BeforeMainActivity.class);
                }
                startActivity(intent);
                StartUpActivity.this.finish();
            }
        },2500);
    }
}
