package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * 消息实体类
 */
public class MessageObject extends DataSupport {
    private int id;

    private String content;

    private Date time;

    private SubClassObject inSubClassObject;

    private UserObject inUserObject;

    private String ownerId;

    private boolean isRead;

    private int type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageObject that = (MessageObject) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public SubClassObject getInSubClassObject() {
        return inSubClassObject;
    }

    public void setInSubClassObject(SubClassObject inSubClassObject) {
        this.inSubClassObject = inSubClassObject;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public UserObject getInUserObject() {
        return inUserObject;
    }

    public void setInUserObject(UserObject inUserObject) {
        this.inUserObject = inUserObject;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
