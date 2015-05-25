package com.flaremars.classmanagers.mainui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.uis.LoginFragment;

public class BeforeMainActivity extends FragmentActivity {

    public static final int CONTAINER_ID = R.id.before_main_activity_container;

    public static final int FRAGMENT_LOGIN = 1;

    public static final int FRAGMENT_REGISTER_ONE = 2;

    public static final int FRAGMENT_REGISTER_TWO = 3;

    public static final int FRAGMENT_GUIDE_PAGE = 4;

    private int curFragment = FRAGMENT_LOGIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

//        Bmob.initialize(this, AppConst.APP_ID);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(CONTAINER_ID,new LoginFragment());
        transaction.commit();
    }

    public void setCurFragment(int fragmentId) {
        this.curFragment = fragmentId;
    }

    private long exitTime;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (curFragment == FRAGMENT_LOGIN || curFragment == FRAGMENT_GUIDE_PAGE) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
                return true;
            }
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                Intent intent = new Intent(this, BeforeMainActivity.class);
                startActivity(intent);
                this.finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class FinishSelfReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("BeforeMainActivity","finishde~");
            if (receiver != null) {
                unregisterReceiver(receiver);
                receiver = null;
            }
            BeforeMainActivity.this.finish();
        }
    }

    private FinishSelfReceiver receiver;

    @Override
    protected void onStart() {
        super.onStart();
        if (receiver == null) {
            receiver = new FinishSelfReceiver();
            registerReceiver(receiver, new IntentFilter("FRAGMENT_TO_FINISH"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }
}
