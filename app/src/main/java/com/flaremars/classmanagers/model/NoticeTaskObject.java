package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * 作为发出者而生的通知对象
 */
public class NoticeTaskObject extends DataSupport {
    private int id;

    private String name;

    private String noticeId;

    private String content;

    private Date time;

    private boolean newFeedback;

    private NoticeGroupObject inNoticeGroup;

    private List<NoticeFeedbackObject> feedbackObjects;

    public List<NoticeFeedbackObject> getFeedbackObjectList() {
        return DataSupport.where("noticetaskobject_id=?",String.valueOf(id)).find(NoticeFeedbackObject.class);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public boolean isNewFeedback() {
        return newFeedback;
    }

    public void setNewFeedback(boolean newFeedback) {
        this.newFeedback = newFeedback;
    }

    //一对多的关系只能这样做
    public NoticeGroupObject getInNoticeGroup() {
        return inNoticeGroup;
    }

    public void setInNoticeGroup(NoticeGroupObject inNoticeGroup) {
        this.inNoticeGroup = inNoticeGroup;
    }

    public List<NoticeFeedbackObject> getFeedbackObjects() {
        return feedbackObjects;
    }

    public void setFeedbackObjects(List<NoticeFeedbackObject> feedbackObjects) {
        this.feedbackObjects = feedbackObjects;
    }

}
