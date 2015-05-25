package com.flaremars.classmanagers.mainui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.flaremars.classmanagers.R;

public class StartUpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        AssetManager assetManager = getAssets();
        Typeface tf = Typeface.createFromAsset(assetManager, "fonts/FZJL.ttf");

        TextView secondTextView = (TextView) findViewById(R.id.second_text_view);
        secondTextView.setTypeface(tf);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(StartUpActivity.this,BeforeMainActivity.class);
                startActivity(intent);

                StartUpActivity.this.finish();
            }
        },3000);
    }
}
