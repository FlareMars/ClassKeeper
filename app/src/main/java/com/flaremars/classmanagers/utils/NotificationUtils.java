package com.flaremars.classmanagers.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.AppConst;

import java.util.List;

/**
 * 新消息通知工具类
 */
public enum NotificationUtils {
    INSTANCE;

    private static final long[] VIBRATE= {0,300,200,300};

    private Context context;

    private int mode = 0;

    private boolean withVoice = true;

    private boolean withVibrate = true;

    private NotificationManager mNotificationManager;

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void isWithVoice(boolean is) {
        this.withVoice = is;
    }

    public void isWithVivbrate(boolean is) {
        this.withVibrate = is;
    }

    public void init(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,Context.MODE_PRIVATE);
        withVibrate = preferences.getBoolean(AppConst.NEED_VIBRATE, true);
        withVoice = preferences.getBoolean(AppConst.NEED_VOICE, true);
        this.context = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
    }

    /**
     *
     */
    public void toNotify(String content) {
        int defaults = 0;
        //如果在聊天界面上，就不提醒了
        if (mode == 0) {
            return;
        }
        if (withVoice) {
            defaults |= Notification.DEFAULT_SOUND;
        }

        if (isTopActivity()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setDefaults(defaults);
            if (withVibrate) {
                builder.setVibrate(VIBRATE);
            }
            mNotificationManager.notify(10, builder.build());
        } else {
            defaults |= Notification.DEFAULT_LIGHTS;
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setDefaults(defaults);
            if (withVibrate) {
                builder.setVibrate(VIBRATE);
            }
            builder.setContentTitle("班级管家");
            builder.setContentText(content);
            builder.setTicker(content);
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.cmicon);
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            mNotificationManager.notify(10, builder.build());
        }
    }

    protected  boolean isTopActivity(){
        String packageName = "com.flaremars.classmanagers";
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
        if(tasksInfo.size() > 0){
           Log.e("NotificationUtils","---------------包名-----------" + tasksInfo.get(0).topActivity.getPackageName());
            //应用程序位于堆栈的顶层
            if(packageName.equals(tasksInfo.get(0).topActivity.getPackageName())){
                return true;
            }
        }
        return false;
    }
}
