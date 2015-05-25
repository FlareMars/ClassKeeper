package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

/**
 * 通知反馈实体类
 */
public class NoticeFeedbackObject extends DataSupport{

    private int id;

    private String whose;

    private String targetPhone;

    private boolean isDone;

    private NoticeTaskObject inNoticeTaskObject;

    private String whoseName;

    public String getWhoseName() {
        return whoseName;
    }

    public void setWhoseName(String whoseName) {
        this.whoseName = whoseName;
    }

    public NoticeTaskObject getInNoticeTaskObject() {
        return inNoticeTaskObject;
    }

    public void setInNoticeTaskObject(NoticeTaskObject inNoticeTaskObject) {
        this.inNoticeTaskObject = inNoticeTaskObject;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWhose() {
        return whose;
    }

    public void setWhose(String whose) {
        this.whose = whose;
    }

    public String getTargetPhone() {
        return targetPhone;
    }

    public void setTargetPhone(String targetPhone) {
        this.targetPhone = targetPhone;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }
}
