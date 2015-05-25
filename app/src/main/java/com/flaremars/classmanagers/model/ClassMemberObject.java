package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

/**
 * 班级对象 成员实体类
 */
public class ClassMemberObject extends DataSupport{

    private int id;

    private String memberName;

    private String memberID;

    private ClassObject inClass;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (o instanceof String) {
            return memberID.equals(o);
        }

        ClassMemberObject object = (ClassMemberObject) o;

        if (id != object.id) return false;
        if (memberName != null ? !memberName.equals(object.memberName) : object.memberName != null)
            return false;
        return !(memberID != null ? !memberID.equals(object.memberID) : object.memberID != null) && !(inClass != null ? !inClass.equals(object.inClass) : object.inClass != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (memberName != null ? memberName.hashCode() : 0);
        result = 31 * result + (memberID != null ? memberID.hashCode() : 0);
        result = 31 * result + (inClass != null ? inClass.hashCode() : 0);
        return result;
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

    public ClassObject getInClass() {
        return inClass;
    }

    public void setInClass(ClassObject inClass) {
        this.inClass = inClass;
    }
}
