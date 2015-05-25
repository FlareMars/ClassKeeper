package com.flaremars.classmanagers.uis;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.UserPersonalInfo;
import com.flaremars.classmanagers.views.Dialog;

import org.litepal.crud.DataSupport;

import java.util.List;

public class CreateClassSuccessFragment extends BaseFragment {

    private String classID;
    private String classCode;
    private String className;
    private String meUserId;

    public static CreateClassSuccessFragment newInstance(String classCode, String className,String classID) {
        CreateClassSuccessFragment fragment = new CreateClassSuccessFragment();
        Bundle args = new Bundle();
        args.putString(AppConst.CLASS_ID, classID);
        args.putString(AppConst.CLASS_NAME, className);
        args.putString(AppConst.CLASS_CODE,classCode);
        fragment.setArguments(args);
        return fragment;
    }

    public CreateClassSuccessFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            classID = getArguments().getString(AppConst.CLASS_ID);
            classCode = getArguments().getString(AppConst.CLASS_CODE);
            className = getArguments().getString(AppConst.CLASS_NAME);
            SharedPreferences preferences = getContainerActivity().getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,Context.MODE_PRIVATE);
            meUserId = preferences.getString(AppConst.USER_ID,"");
        }
    }

    @Override
    void backAction() {
        Intent intent = new Intent(getContainerActivity(),MainActivity.class);
        startActivity(intent);
        Intent finishActivityBroadCast = new Intent("FRAGMENT_TO_FINISH");
        getContainerActivity().sendBroadcast(finishActivityBroadCast);
        getContainerActivity().finish();
    }

    @Override
    String fragmentTitle() {
        return "创建班级";
    }

    @Override
    List<Integer> setActionViews() {
        return null;
    }

    private boolean isUpdated = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_class_success, container, false);
        ((TextView)view.findViewById(R.id.fragment_create_success_id)).setText(classCode);
        ((TextView)view.findViewById(R.id.fragment_create_success_name)).setText(className);

        if (MainActivity.BASE_GLOBAL_DATA.getCurClassID().equals("")) {
            isUpdated = true;
            updateCurClass();
        }

        final UserPersonalInfo userPersonalInfo = DataSupport.where("userId=?",meUserId).find(UserPersonalInfo.class).get(0);
        final ClassObject classObject = DataSupport.where("classID=?", classID).find(ClassObject.class).get(0);
        if (userPersonalInfo.getAcademy() == null || userPersonalInfo.getAcademy().equals("")) {
            String format = "您所在的学校/学院信息为:\n%s\n%s";
            final Dialog dialog = new Dialog(getContainerActivity(),"修改信息",
                    String.format(format,classObject.getInSchool(),classObject.getInAcademy()));
            dialog.addCancelButton("取消");
            dialog.setButtonAcceptText("确认修改");
            dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AVQuery<AVObject> userInfoQuery = new AVQuery<>("CMUserInfo");
                    userInfoQuery.whereEqualTo("userId", meUserId);
                    userInfoQuery.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list, AVException e) {
                            if (e == null) {
                                if (list.size() > 0) {
                                    AVObject infoUpdate = list.get(0);
                                    infoUpdate.put("school", classObject.getInSchool());
                                    infoUpdate.put("academy", classObject.getInAcademy());
                                    infoUpdate.saveInBackground();

                                    userPersonalInfo.setSchool(classObject.getInSchool());
                                    userPersonalInfo.setAcademy(classObject.getInAcademy());
                                    userPersonalInfo.update(userPersonalInfo.getId());
                                }
                            }
                        }
                    });
                }
            });
            dialog.show();
        }

        view.findViewById(R.id.fragment_create_success_confirm_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isUpdated) {
                    updateCurClass();
                }
                getContainerActivity().replaceFragment(ClassInfoFragment.newInstance(classID),false);
                Intent finishActivityBroadCast = new Intent("FRAGMENT_TO_FINISH");
                getContainerActivity().sendBroadcast(finishActivityBroadCast);
            }
        });
        return view;
    }

    private void updateCurClass() {
        MainActivity.BASE_GLOBAL_DATA.setCurClassID(classID);
        MainActivity.BASE_GLOBAL_DATA.setCurClassName(className);
        SharedPreferences sharedPreferences = getContainerActivity().getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppConst.CUR_CLASS_ID, classID);
        editor.putString(AppConst.CUR_CLASS_NAME, className);
        editor.apply();

        final UserPersonalInfo userPersonalInfo = DataSupport.where("userId=?", meUserId).find(UserPersonalInfo.class).get(0);
        userPersonalInfo.setCurClassId(classID);
        userPersonalInfo.setCurClassName(className);
        userPersonalInfo.update(userPersonalInfo.getId());
    }
}
