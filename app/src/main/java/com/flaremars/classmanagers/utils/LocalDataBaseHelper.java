package com.flaremars.classmanagers.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.model.UserPersonalInfo;

/**
 * Created by FlareMars on 2015/5/30.
 * 作为本地数据库操作统一工作类
 */
public enum  LocalDataBaseHelper {

    INSTANCE;

    public interface WorkingDoneListener {
        void onWorkingDone();
    }

    public void registerNewUser(AVUser newUser, String realName,WorkingDoneListener listener) {

        String userObjectId = newUser.getObjectId();

        UserObject userObject = new UserObject();
        userObject.setUserId(userObjectId);
        userObject.setImgId("");
        userObject.setUserRealName(realName);
        userObject.setUserName(newUser.getUsername());
        userObject.save();

        UserPersonalInfo personalInfo = new UserPersonalInfo();
        personalInfo.setUserId(userObjectId);
        personalInfo.setAcademy("");
        personalInfo.setSchool("");
        personalInfo.setBirthday(null);
        personalInfo.setSex("男");
        personalInfo.save();

        CMApplication.openIMClient(userObjectId);

        listener.onWorkingDone();
    }

    public UserObject saveUserData(AVObject userInfo,String userId,String userName) {

        //save UserObject
        UserObject newUser = new UserObject();
        newUser.setImgId(userInfo.getString("headerImg"));
        newUser.setUserRealName(userName);
        newUser.setUserId(userId);
        newUser.setUserName(userInfo.getString("phone"));
        newUser.saveThrows();

        //save UserPersonalInfo
        UserPersonalInfo personalInfo = new UserPersonalInfo();
        personalInfo.setAcademy(userInfo.getString("academy"));
        personalInfo.setBirthday(userInfo.getDate("birthday"));
        personalInfo.setSchool(userInfo.getString("school"));
        personalInfo.setSex(userInfo.getString("sex"));
        personalInfo.setUserId(userId);
        personalInfo.saveThrows();

        return newUser;
    }

    public void changeCurClass(Context context, final String classId, final String className,UserPersonalInfo userPersonalInfo) {
        MainActivity.BASE_GLOBAL_DATA.setCurClassID(classId);
        MainActivity.BASE_GLOBAL_DATA.setCurClassName(className);

        final SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppConst.CUR_CLASS_ID, classId);
        editor.putString(AppConst.CUR_CLASS_NAME, className);
        editor.apply();

        userPersonalInfo.setCurClassId(classId);
        userPersonalInfo.setCurClassName(className);
        userPersonalInfo.update(userPersonalInfo.getId());

        //更新用户信息中的当前班级
        final AVQuery<AVObject> query = new AVQuery<>("UserToCurClass");
        query.getInBackground(sharedPreferences.getString(AppConst.USER_TO_CURCLASS, ""), new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (e == null) {
                    avObject.put("curClassId", classId);
                    avObject.put("curClassName", className);
                    avObject.saveInBackground();
                }
            }
        });
    }
}
