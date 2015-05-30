package com.flaremars.classmanagers.utils;

import com.avos.avoscloud.AVUser;
import com.flaremars.classmanagers.CMApplication;
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
        String username = newUser.getUsername();

        //save UserObject
        UserObject userObject = new UserObject();
        userObject.setUserId(userObjectId);
        userObject.setImgId("");
        userObject.setUserRealName(realName);
        userObject.setUserName(newUser.getUsername());
        userObject.save();

        //save UserPersonalInfo
        UserPersonalInfo personalInfo = new UserPersonalInfo();
        personalInfo.setUserId(userObjectId);
        personalInfo.setAcademy("");
        personalInfo.setSchool("");
        personalInfo.setBirthday(null);
        personalInfo.setSex("男");
        personalInfo.save();

        CMApplication.openIMClient(username);

        listener.onWorkingDone();
    }
}
