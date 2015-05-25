package com.flaremars.classmanagers.model;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * 网络消息数据载体
 */
public class DataWrapper {

    private String senderId;

    private String classId;

    private String subClassId;

    private List<String> targetIds;

    private JSONObject content;

    private Date time;

    public DataWrapper(String senderId, String classId, String subClassId, List<String> targetIds, JSONObject content) {
        this.senderId = senderId;
        this.classId = classId;
        this.subClassId = subClassId;
        this.targetIds = targetIds;
        this.content = content;
        time = new Date();
    }

    public String getSenderId() {
        return senderId;
    }

    public String getClassId() {
        return classId;
    }

    public String getSubClassId() {
        return subClassId;
    }

    public List<String> getTargetIds() {
        return targetIds;
    }

    public JSONObject getContent() {
        return content;
    }

    public Date getTime() {
        return time;
    }
}