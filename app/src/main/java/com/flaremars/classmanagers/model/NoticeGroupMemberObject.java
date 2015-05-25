package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

/**
 * 通知组对象 成员实体类
 */
public class NoticeGroupMemberObject extends DataSupport{

    private int id;

    private String memberName;

    private String memberID;

    private NoticeGroupObject inNoticeGroup;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoticeGroupMemberObject that = (NoticeGroupMemberObject) o;

        return !(memberID != null ? !memberID.equals(that.memberID) : that.memberID != null);

    }

    @Override
    public int hashCode() {
        return memberID != null ? memberID.hashCode() : 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public NoticeGroupObject getInNoticeGroup() {
        return inNoticeGroup;
    }

    public void setInNoticeGroup(NoticeGroupObject inNoticeGroup) {
        this.inNoticeGroup = inNoticeGroup;
    }
}
