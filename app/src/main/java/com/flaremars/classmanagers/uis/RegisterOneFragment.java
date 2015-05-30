package com.flaremars.classmanagers.uis;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.SignUpCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.BeforeMainActivity;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.MyRippleView;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterOneFragment extends Fragment {

    private static final int MOBILE_IS_ALREADY_TOKEN = 214;

    private static final int START_TO_COUNT = 0;

    private static final int PROCESS_IN_COUNT = 1;

    private static final int RESET_COUNT = 2;

    private EditText phoneNumberEt;

    private EditText identifyingCodeEt;

    private TextView getIdentifyingCodeTvBtn;

    private MyRippleView nextBtn;

    private BeforeMainActivity context;

    private String tempUserId;

    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

    private int tempCounter = 60;

    private static class  ResendCountHandler extends Handler {
        private WeakReference<RegisterOneFragment> parent;

        public ResendCountHandler(RegisterOneFragment parent) {
            this.parent = new WeakReference<>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            RegisterOneFragment container = parent.get();
            switch (msg.what) {
                case START_TO_COUNT:
                    container.getIdentifyingCodeTvBtn.setBackgroundResource(R.color.font_little_title);
                    container.getIdentifyingCodeTvBtn.setText("( 60 )秒后可重发验证码");
                    container.tempCounter = 60;
                    break;
                case PROCESS_IN_COUNT:
                    container.tempCounter--;
                    String format = "( %d )秒后可重发验证码";
                    container.getIdentifyingCodeTvBtn.setText(String.format(format, container.tempCounter));

                    if (container.tempCounter == 0) {
                        container.tempCounter = 60;
                        container.scheduledThreadPool.shutdown();
                        container.getIdentifyingCodeTvBtn.setBackgroundResource(R.drawable.selector_blue_btn);
                        container.getIdentifyingCodeTvBtn.setText("点击发送验证码");
                    }
                    break;
                case RESET_COUNT:
                    container.tempCounter = 60;
                    container.scheduledThreadPool.shutdown();
                    container.getIdentifyingCodeTvBtn.setBackgroundResource(R.drawable.selector_blue_btn);
                    container.getIdentifyingCodeTvBtn.setText("点击发送验证码");
                    break;
                default:
            }
        }
    }

    private ResendCountHandler handler;

    public RegisterOneFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = (BeforeMainActivity)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        handler = new ResendCountHandler(this);
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        phoneNumberEt = (EditText) view.findViewById(R.id.et_register_phone);
        identifyingCodeEt = (EditText) view.findViewById(R.id.et_register_identifying_code);
        getIdentifyingCodeTvBtn = (TextView) view.findViewById(R.id.tv_btn_get_identifying_code);
        nextBtn = (MyRippleView) view.findViewById(R.id.mrv_next);

        view.findViewById(R.id.tv_btn_register_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextBtn.animateRipple();
            }
        });

        getIdentifyingCodeTvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tempCounter != 60) {
                    return;
                }

                final String phoneNumber = phoneNumberEt.getText().toString();
                Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(17[0,7]))\\d{8}$");
                Matcher m = p.matcher(phoneNumber);
                if (!m.matches()) {
                    Toast.makeText(context, "手机号码有误", Toast.LENGTH_LONG).show();
                    return;
                }

                final AVUser user = new AVUser();
                user.setUsername(phoneNumber);
                user.setPassword("123456");
                user.setMobilePhoneNumber(phoneNumber);
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) { //普通情况下，根据注册时的手机号码，直接发送验证码
                            tempUserId = user.getUsername();
                            NormalUtils.INSTANCE.showToast(context, "已发出验证码，请注意查收");

                            startToCount();
                        } else {
                            Log.e("TAG", e.getMessage());
                            if (e.getCode() == MOBILE_IS_ALREADY_TOKEN) {
                                if (!user.isMobilePhoneVerified()) { //如果是二次发送验证码，则进入此代码块
                                    AVUser.requestMobilePhoneVerifyInBackground(phoneNumber, new RequestMobileCodeCallback() {
                                        @Override
                                        public void done(AVException e) {
                                            if (e != null) {
                                                NormalUtils.INSTANCE.showToast(context, "发出验证码失败 : " + e.getCode() + " " + e.getMessage());
                                            } else {
                                                tempUserId = user.getUsername();
                                                NormalUtils.INSTANCE.showToast(context, "已发出验证码，请注意查收");
                                                startToCount();
                                            }
                                        }
                                    });
                                } else {
                                    NormalUtils.INSTANCE.showToast(context,"不能重复注册");
                                }
                            } else {
                                NormalUtils.INSTANCE.showError(context, e);
                            }
                        }
                    }
                });
            }
        });

        nextBtn.setOnAnimationEndListener(new MyRippleView.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                String identifyingCode = identifyingCodeEt.getText().toString();

                if (identifyingCode.equals("")) {
                    NormalUtils.INSTANCE.showToast(context, "验证码不能为空");
                    return;
                }

                if (tempUserId == null || tempUserId.equals("")) {
                    tempUserId = phoneNumberEt.getText().toString();
                }

                AVUser.verifyMobilePhoneInBackground(identifyingCode, new AVMobilePhoneVerifyCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e != null) {
                            NormalUtils.INSTANCE.showToast(context, "验证码错误~");
                        } else {
                            FragmentManager fragmentManager = context.getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out,R.anim.push_left_in, R.anim.push_left_out);
                            transaction.replace(BeforeMainActivity.CONTAINER_ID, RegisterTowFragment.newInstance(phoneNumberEt.getText().toString(), tempUserId));
                            transaction.commit();

                            context.setCurFragment(BeforeMainActivity.FRAGMENT_REGISTER_TWO);
                        }
                    }
                });

            }
        });
        return view;
    }

    private void startToCount() {
        //开始倒数60秒
        Message msg = new Message();
        msg.what = START_TO_COUNT;
        handler.sendMessage(msg);
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = PROCESS_IN_COUNT;
                handler.sendMessage(msg);
            }
        },1,1, TimeUnit.SECONDS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!scheduledThreadPool.isShutdown()) {
            scheduledThreadPool.shutdown();
        }
    }
}
