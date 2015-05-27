package com.flaremars.classmanagers.uis;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.MyRippleView;

public class ResetPWOneFragment extends Fragment {

    private ResetPasswordActivity context;

    private boolean isProcessing = false;

    public ResetPWOneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = (ResetPasswordActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_pwone, container, false);
        final MyRippleView nextBtn = (MyRippleView) view.findViewById(R.id.mrv_next);
        final TextView getCodeBtn = (TextView) view.findViewById(R.id.tv_btn_get_identifying_code);
        final EditText phoneEditText = (EditText) view.findViewById(R.id.et_reset_phone);
        final EditText smsCodeEditText = (EditText) view.findViewById(R.id.et_reset_identifying_code);

        getCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isProcessing) {
                    return;
                }
                String phone = phoneEditText.getText().toString();
                if (phone.length() != 11) {
                    NormalUtils.INSTANCE.showToast(context,"手机号码有误，请检查输入");
                    return;
                }
                isProcessing = true;
                AVUser.requestPasswordResetBySmsCodeInBackground(phone, new RequestMobileCodeCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            NormalUtils.INSTANCE.showToast(context,"验证码已发出，请尽快完成验证");
                        } else {
                            NormalUtils.INSTANCE.showError(context,e);
                        }
                    }
                });
            }
        });

        view.findViewById(R.id.tv_btn_reset_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextBtn.animateRipple();
            }
        });

        nextBtn.setOnAnimationEndListener(new MyRippleView.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                String smsCode = smsCodeEditText.getText().toString();
                if (smsCode.equals("")) {
                    NormalUtils.INSTANCE.showToast(context,"验证码不能为空");
                    return;
                }

                FragmentManager fragmentManager = context.getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_left_in, R.anim.push_left_out);
                transaction.replace(R.id.content_container,ResetPasswordTwoFragment.newInstance(smsCode));
                transaction.commit();
            }
        });
        return view;
    }


}
