package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户实体类
 */
public class UserObject extends DataSupport{

    private int id;

    //用户的真实姓名
    private String userRealName;

    //用户的手机
    private String userName;

    //用户在服务器上的id
    private String userId;

    //用户的头像id,即idFromServer
    private String imgId;

    //单聊会话ID
    private String conversationId;

    //本用户与此用户的聊天记录
    private List<MessageObject> messageObjects = new ArrayList<>();

    private List<RecentMessageObject> recentMessageObjects = new ArrayList<>();

    public List<RecentMessageObject> getRecentMessageObjects() {
        return recentMessageObjects;
    }

    public List<RecentMessageObject> getRecentMessageObjectList() {
        return DataSupport.where("userobject_id=?",String.valueOf(id)).find(RecentMessageObject.class);
    }

    public void setRecentMessageObjects(List<RecentMessageObject> recentMessageObjects) {
        this.recentMessageObjects = recentMessageObjects;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId;
    }

    public List<MessageObject> getMessageObjectList() {
        return DataSupport.where("userobject_id=?",String.valueOf(id)).find(MessageObject.class);
    }

    public List<MessageObject> getMessageObjects() {
        return messageObjects;
    }

    public void setMessageObjects(List<MessageObject> messageObjects) {
        this.messageObjects = messageObjects;
    }
}
