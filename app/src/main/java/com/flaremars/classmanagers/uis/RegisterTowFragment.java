package com.flaremars.classmanagers.uis;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.UpdatePasswordCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.BeforeMainActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.model.UserPersonalInfo;
import com.flaremars.classmanagers.utils.LocalDataBaseHelper;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.MyRippleView;

import java.util.Date;

public class RegisterTowFragment extends Fragment {

    private static final String PARAM_PHONE_NUMBER = "param_phone_number";

    private static final String PARAM_USER_ID = "param_user_id";

    private EditText realnameEt;

    private EditText passwordEt;

    private MyRippleView registerDoneBtn;

    private String phoneNum;

    private String userId;

    private BeforeMainActivity context;

    public static RegisterTowFragment newInstance(String phoneNumber,String userId) {
        RegisterTowFragment instance = new RegisterTowFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_PHONE_NUMBER, phoneNumber);
        args.putString(PARAM_USER_ID,userId);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = (BeforeMainActivity)activity;
    }

    public RegisterTowFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phoneNum = getArguments().getString(PARAM_PHONE_NUMBER);
            userId = getArguments().getString(PARAM_USER_ID);
        }
    }

    private boolean isPasswordCorrect = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_next, container, false);
        realnameEt = (EditText) view.findViewById(R.id.et_register_realname);
        passwordEt = (EditText) view.findViewById(R.id.et_register_password);
        EditText confirmPasswordEt = (EditText) view.findViewById(R.id.et_register_password_confirm);
        registerDoneBtn = (MyRippleView) view.findViewById(R.id.mrv_done);

        final ImageView wrongNotice = (ImageView) view.findViewById(R.id.tv_notice_password_wrong);
        wrongNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NormalUtils.INSTANCE.showToast(context, "两次密码不一致~");
            }
        });

        confirmPasswordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(passwordEt.getText().toString())) {
                    wrongNotice.setVisibility(View.GONE);
                    isPasswordCorrect = true;
                } else {
                    wrongNotice.setVisibility(View.VISIBLE);
                    isPasswordCorrect = false;
                }
            }
        });

        view.findViewById(R.id.tv_btn_register_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDoneBtn.animateRipple();
            }
        });

        registerDoneBtn.setOnAnimationEndListener(new MyRippleView.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {

                final String username = phoneNum;
                final String password = passwordEt.getText().toString();
                final String realname = realnameEt.getText().toString();
                if (password.equals("") || realname.equals("")) {
                    NormalUtils.INSTANCE.showToast(context, "密码/真实姓名不能为空");
                    return;
                }
                if (isPasswordCorrect) {
                    AVUser.logInInBackground(userId, "123456", new LogInCallback<AVUser>() {
                        @Override
                        public void done(final AVUser avUser, AVException e) {
                            if (e == null) {
                                avUser.updatePasswordInBackground("123456", password, new UpdatePasswordCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        if (e == null) {
                                            final AVObject userToCurClass = new AVObject("UserToCurClass");
                                            final String userId = avUser.getObjectId();
                                            userToCurClass.put("userId", userId);
                                            userToCurClass.put("curClassId", "");
                                            userToCurClass.put("curClassName", "");
                                            userToCurClass.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(AVException e) {
                                                    if (e == null) {

                                                        LocalDataBaseHelper.INSTANCE.registerNewUser(avUser, realname, new LocalDataBaseHelper.WorkingDoneListener() {
                                                            @Override
                                                            public void onWorkingDone() {

                                                                //保存用于别人查询的数据对象
                                                                AVObject userInfo = new AVObject("CMUserInfo");
                                                                userInfo.put("userId", userId);
                                                                userInfo.put("sex", "男");
                                                                userInfo.put("headerImg", "");
                                                                userInfo.put("phone", avUser.getUsername());
                                                                userInfo.put("realName", realname);
                                                                userInfo.put("academy", "");
                                                                userInfo.put("school", "");
                                                                userInfo.put("birthday", new Date());
                                                                userInfo.saveInBackground();

                                                                //save base_info to share_preference
                                                                SharedPreferences preferences = context.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
                                                                SharedPreferences.Editor editor = preferences.edit();
                                                                editor.clear().apply();  //清空当前preference的数据，以免造成不必要的用户数据混乱
                                                                editor = preferences.edit();
                                                                editor.putString(AppConst.USER_REAL_NAME, realname);
                                                                editor.putString(AppConst.USER_ID,userId);
                                                                editor.putString(AppConst.USER_NAME, username);
                                                                editor.putString(AppConst.USER_PASSWORD, password);
                                                                editor.putString(AppConst.USER_TO_CURCLASS, userToCurClass.getObjectId());
                                                                editor.apply();

                                                                NormalUtils.INSTANCE.showToast(context, "注册成功~");

                                                                //jump to LoginFragment
                                                                FragmentManager fragmentManager = context.getSupportFragmentManager();
                                                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                                                transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_left_in, R.anim.push_left_out);
                                                                transaction.replace(BeforeMainActivity.CONTAINER_ID, new LoginFragment());
                                                                transaction.commit();
                                                                context.setCurFragment(BeforeMainActivity.FRAGMENT_LOGIN);
                                                            }
                                                        });

                                                    } else {
                                                        NormalUtils.INSTANCE.showErrorLog(context, e);
                                                    }
                                                }
                                            });
                                        } else {
                                            NormalUtils.INSTANCE.showErrorLog(context, e);
                                        }
                                    }
                                });
                            } else {
                                NormalUtils.INSTANCE.showErrorLog(context, e);
                            }
                        }
                    });
                }
            }
        });
        return view;
    }
}
