package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 子群实体类
 */
public class SubClassObject extends DataSupport{
    private int id;

    private String subClassName;

    private String subClassID;

    private Date time;

    private ClassObject inClass;

    private List<SubClassMemberObject> members = new ArrayList<>();

    private List<MessageObject> messageObjects = new ArrayList<>();

    public List<SubClassMemberObject> getMemberList() {
        return DataSupport.where("subclassobject_id=?",String.valueOf(id)).find(SubClassMemberObject.class);
    }

    public List<SubClassMemberObject> getMembers() {
        return members;
    }

    public void setMembers(List<SubClassMemberObject> members) {
        this.members = members;
    }

    public List<MessageObject> getMessageObjects() {
        return messageObjects;
    }

    public void setMessageObjects(List<MessageObject> messageObjects) {
        this.messageObjects = messageObjects;
    }

    public ClassObject getInClass() {
        return inClass;
    }

    public void setInClass(ClassObject inClass) {
        this.inClass = inClass;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubClassName() {
        return subClassName;
    }

    public void setSubClassName(String subClassName) {
        this.subClassName = subClassName;
    }

    public String getSubClassID() {
        return subClassID;
    }

    public void setSubClassID(String subClassID) {
        this.subClassID = subClassID;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

}
