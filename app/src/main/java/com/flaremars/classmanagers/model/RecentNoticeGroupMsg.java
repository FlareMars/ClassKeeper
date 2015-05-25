package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * 最近的通知组的信息
 */
public class RecentNoticeGroupMsg extends DataSupport{

    private int id;

    private NoticeGroupObject noticeGroup;

    private String content;

    private int noReadNumber;

    private Date time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public NoticeGroupObject getNoticeGroup() {
        return noticeGroup;
    }

    public void setNoticeGroup(NoticeGroupObject noticeGroup) {
        this.noticeGroup = noticeGroup;
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

    public int getNoReadNumber() {
        return noReadNumber;
    }

    public void setNoReadNumber(int noReadNumber) {
        this.noReadNumber = noReadNumber;
    }
}
