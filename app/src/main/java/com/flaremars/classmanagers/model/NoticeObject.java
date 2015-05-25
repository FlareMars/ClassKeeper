package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 *
 */
public class NoticeObject extends DataSupport{

    private int id;

    private String name;

    private String content;

    private String nameOfSender;

    private String idOfSender;

    private String noticeId;

    private Date time;

    private boolean isRead;

    public String getIdOfSender() {
        return idOfSender;
    }

    public void setIdOfSender(String idOfSender) {
        this.idOfSender = idOfSender;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNameOfSender() {
        return nameOfSender;
    }

    public void setNameOfSender(String nameOfSender) {
        this.nameOfSender = nameOfSender;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }
}
