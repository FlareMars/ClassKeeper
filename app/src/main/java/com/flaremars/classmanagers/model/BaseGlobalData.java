package com.flaremars.classmanagers.model;

/**
 * 存储常用基础数据的静态全局数据类
 */
public class BaseGlobalData {
    private String userName;

    private String userRealName;

    private String userID;

    private String userHeaderId;

    private String curClassName;

    private String curClassID;

    public BaseGlobalData() {
        userName = "";
        userID = "";
        curClassID = "";
        curClassName = "";
    }

    public String getUserHeaderId() {
        return userHeaderId;
    }

    public void setUserHeaderId(String userHeaderId) {
        this.userHeaderId = userHeaderId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserID() {
        return userID;
    }

    public String getCurClassName() {
        return curClassName;
    }

    public String getCurClassID() {
        return curClassID;
    }

    public void setCurClassName(String curClassName) {
        this.curClassName = curClassName;
    }

    public void setCurClassID(String curClassID) {
        this.curClassID = curClassID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }
}
