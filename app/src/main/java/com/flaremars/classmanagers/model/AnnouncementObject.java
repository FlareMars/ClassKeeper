package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * 公告实体类
 */
public class AnnouncementObject extends DataSupport{
    private int id;

    private String title;

    private String content;

    private Date deadline;

    private String nameOfSender;

    private String idOfSender;

    private ClassObject inClass;

    private String networkId;

    public ClassObject getInClass() {
        return inClass;
    }

    public void setInClass(ClassObject inClass) {
        this.inClass = inClass;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getNameOfSender() {
        return nameOfSender;
    }

    public void setNameOfSender(String nameOfSender) {
        this.nameOfSender = nameOfSender;
    }

    public String getIdOfSender() {
        return idOfSender;
    }

    public void setIdOfSender(String idOfSender) {
        this.idOfSender = idOfSender;
    }
}
