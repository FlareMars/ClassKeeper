package com.flaremars.classmanagers.uis;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.UpdatePasswordCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.MyRippleView;

public class ResetPasswordTwoFragment extends Fragment {

    private static String SMSCODE_PARAM = "smscode_param";

    private String smsCode;

    public ResetPasswordTwoFragment() {
        // Required empty public constructor
    }

    public static ResetPasswordTwoFragment newInstance(String smsCode) {
        ResetPasswordTwoFragment fragment = new ResetPasswordTwoFragment();
        Bundle argument = new Bundle();
        argument.putString(SMSCODE_PARAM, smsCode);
        fragment.setArguments(argument);
        return  fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle argument = getArguments();
        if (argument != null) {
            smsCode = argument.getString(SMSCODE_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password_two, container, false);
        final MyRippleView doneBtn = (MyRippleView) view.findViewById(R.id.mrv_done);
        final EditText newPasswordEt = (EditText) view.findViewById(R.id.et_reset_new_password);
        final EditText confirmPasswordEt = (EditText) view.findViewById(R.id.et_reset_confirm_password);

        view.findViewById(R.id.tv_btn_reset_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneBtn.animateRipple();
            }
        });

        doneBtn.setOnAnimationEndListener(new MyRippleView.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                String newPassword = newPasswordEt.getText().toString();
                String confirmPassword = confirmPasswordEt.getText().toString();
                if (newPassword.equals("") || confirmPassword.equals("")) {
                    NormalUtils.INSTANCE.showToast(getActivity(),"输入不能为空");
                    return;
                }
                if (!confirmPassword.equals(newPassword)) {
                    NormalUtils.INSTANCE.showToast(getActivity(),"两次输入的密码不一致");
                    return;
                }

                AVUser.resetPasswordBySmsCodeInBackground(smsCode, newPassword, new UpdatePasswordCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            NormalUtils.INSTANCE.showToast(CMApplication.GLOBAL_CONTEXT,"密码重置成功，请尝试登陆");
                            getActivity().finish();
                        } else {
                            NormalUtils.INSTANCE.showError(getActivity(),e);
                        }
                    }
                });
            }
        });
        return view;
    }


}
