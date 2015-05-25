package com.flaremars.classmanagers.uis;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterOneFragment extends Fragment {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);

    private EditText phoneNumberEt;

    private EditText identifyingCodeEt;

    private MyRippleView nextBtn;

    private BeforeMainActivity context;

    private String tempUserId;

    private boolean isProgress = false;

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
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        phoneNumberEt = (EditText) view.findViewById(R.id.et_register_phone);
        identifyingCodeEt = (EditText) view.findViewById(R.id.et_register_identifying_code);
        final TextView getIdentifyingCodeTvBtn = (TextView) view.findViewById(R.id.tv_btn_get_identifying_code);
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

                if (isProgress) {
                    return;
                }

                isProgress = true;
                final String phoneNumber = phoneNumberEt.getText().toString();
                Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(17[0,7]))\\d{8}$");
                Matcher m = p.matcher(phoneNumber);
                if (!m.matches()) {
                    Toast.makeText(context, "手机号码有误", Toast.LENGTH_LONG).show();
                    isProgress = false;
                    return;
                }

                final AVUser user = new AVUser();
                user.setUsername(phoneNumber);
                user.setPassword("123456");
                user.setMobilePhoneNumber(phoneNumber);
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            tempUserId = user.getUsername();
                            NormalUtils.INSTANCE.showToast(context, "请输入接收到的短信验证码");
//                            AVUser.requestMobilePhoneVerifyInBackground(phoneNumber, new RequestMobileCodeCallback() {
//                                @Override
//                                public void done(AVException e) {
//                                    if (e != null) {
//                                        NormalUtils.INSTANCE.showToast(context, "发出验证码失败 : " + e.getCode() + " " + e.getMessage());
//                                    } else {
//
//                                    }
//                                }
//                            });
                        } else {
                            Log.e("TAG", e.getMessage());
                            isProgress = false;
                            NormalUtils.INSTANCE.showError(context, e);
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

//                FragmentManager fragmentManager = context.getSupportFragmentManager();
//                FragmentTransaction transaction = fragmentManager.beginTransaction();
//                transaction.replace(BeforeMainActivity.CONTAINER_ID, RegisterTowFragment.newInstance(phoneNumberEt.getText().toString(), tempUserId));
////                transaction.addToBackStack(null);
//                transaction.commit();

//                context.setCurFragment(BeforeMainActivity.FRAGMENT_REGISTER_TWO);

                AVUser.verifyMobilePhoneInBackground(identifyingCode, new AVMobilePhoneVerifyCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e != null) {
                            NormalUtils.INSTANCE.showToast(context, "验证码错误~");
                            isProgress = false;
                        } else {
                            FragmentManager fragmentManager = context.getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.replace(BeforeMainActivity.CONTAINER_ID, RegisterTowFragment.newInstance(phoneNumberEt.getText().toString(), tempUserId));
                            transaction.addToBackStack(null);
                            transaction.commit();

                            context.setCurFragment(BeforeMainActivity.FRAGMENT_REGISTER_TWO);
                        }
                    }
                });


            }
        });
        return view;
    }


}
