package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

/**
 * 子群对象 成员实体类
 */
public class SubClassMemberObject extends DataSupport{

    private int id;

    private String memberName;

    private String memberID;

    private SubClassObject inSubClass;

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public SubClassObject getInSubClass() {
        return inSubClass;
    }

    public void setInSubClass(SubClassObject inSubClass) {
        this.inSubClass = inSubClass;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
