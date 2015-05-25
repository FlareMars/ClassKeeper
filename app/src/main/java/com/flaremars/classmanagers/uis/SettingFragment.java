package com.flaremars.classmanagers.uis;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.BeforeMainActivity;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.service.CMService;
import com.flaremars.classmanagers.utils.NotificationUtils;

import java.util.List;


public class SettingFragment extends BaseFragment {

    private SharedPreferences preferences;

    public SettingFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settng, container, false);
        LinearLayout logout = (LinearLayout) view.findViewById(R.id.setting_logout);
        TextView aboutTvBtn = (TextView) view.findViewById(R.id.setting_about);
        aboutTvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutDialog.getAboutDialog().show(getContainerActivity().getSupportFragmentManager(),"about_dialog");
            }
        });

        TextView feedbackTvBtn = (TextView) view.findViewById(R.id.setting_feedback);
        feedbackTvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContainerActivity(), FeedbackActivity.class);
                startActivity(intent);
            }
        });

        TextView checkUpdateTbBtn = (TextView) view.findViewById(R.id.setting_check_update);
        checkUpdateTbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        preferences = getContainerActivity().getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean isShowCornerSignal = preferences.getBoolean(AppConst.CORNER_NOTICE, true);
        boolean needVoice = preferences.getBoolean(AppConst.NEED_VOICE, true);
        //限定的几个声音
        int voiceCategory = preferences.getInt(AppConst.CATEGORY_VOICE, 0);
        boolean isVibrate = preferences.getBoolean(AppConst.NEED_VIBRATE, true);

        final CheckBox checkBox1 = (CheckBox) view.findViewById(R.id.cb_setting_set1);
        final CheckBox checkBox2 = (CheckBox) view.findViewById(R.id.cb_setting_set2);
        final CheckBox checkBox3 = (CheckBox) view.findViewById(R.id.cb_setting_set3);

        checkBox1.setChecked(isShowCornerSignal);
        checkBox2.setChecked(needVoice);
        checkBox3.setChecked(isVibrate);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AVUser.logOut();
                Intent intent = new Intent(getContainerActivity(), BeforeMainActivity.class);
                startActivity(intent);
                getContainerActivity().stopService(new Intent(getContainerActivity(), CMService.class));

                MainActivity.MainActivityInstance.finish();
                getContainerActivity().finish();
            }
        });

        final TextView voiceSelector = (TextView) view.findViewById(R.id.tv_setting_voice);
        voiceSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹框选择声音
                Log.e("TAG","onClick");

                //选择完后设置铃声
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(AppConst.CATEGORY_VOICE,0);
                        editor.apply();
            }
        });
        if (!needVoice) {
            voiceSelector.setEnabled(false);
        } else {
            voiceSelector.setEnabled(true);
        }

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(AppConst.CORNER_NOTICE, checkBox1.isChecked());
                editor.putBoolean(AppConst.NEED_VOICE, checkBox2.isChecked());
                editor.putBoolean(AppConst.NEED_VIBRATE, checkBox3.isChecked());
                voiceSelector.setEnabled(checkBox2.isChecked());
                editor.apply();

                NotificationUtils.INSTANCE.isWithVivbrate(checkBox3.isChecked());
                NotificationUtils.INSTANCE.isWithVoice(checkBox2.isChecked());
            }
        };

        checkBox1.setClickable(false);
        checkBox2.setClickable(false);
        checkBox3.setClickable(false);

        checkBox1.setOnCheckedChangeListener(listener);
        checkBox2.setOnCheckedChangeListener(listener);
        checkBox3.setOnCheckedChangeListener(listener);

        view.findViewById(R.id.ll_btn_voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox2.setChecked(!checkBox2.isChecked());
            }
        });

        view.findViewById(R.id.ll_btn_vibrate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox3.setChecked(!checkBox3.isChecked());
            }
        });

        return view;
    }


    @Override
    void backAction() {
        Intent intent = new Intent(getContainerActivity(), MainActivity.class);

        startActivity(intent);
    }

    @Override
    String fragmentTitle() {
        return "设置";
    }

    @Override
    List<Integer> setActionViews() {
        return null;
    }
}
