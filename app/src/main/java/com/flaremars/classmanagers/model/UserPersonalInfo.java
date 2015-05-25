package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * 记录用户所有的基础信息数据
 */
public class UserPersonalInfo extends DataSupport{

    private int id;

    private String userId;

    private String school;

    private String academy;

    private Date birthday;

    private String sex;

    private String curClassId;

    private String curClassName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCurClassId() {
        return curClassId;
    }

    public void setCurClassId(String curClassId) {
        this.curClassId = curClassId;
    }

    public String getCurClassName() {
        return curClassName;
    }

    public void setCurClassName(String curClassName) {
        this.curClassName = curClassName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAcademy() {
        return academy;
    }

    public void setAcademy(String academy) {
        this.academy = academy;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }
}
