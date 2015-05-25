package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * 作为发出者而生的信息录制对象
 */
public class ExcelTaskObject extends DataSupport{
    private int id;

    private String name;

    private String excelId;

    private String questions;

    private Date time;

    private boolean newFeedback;

    private NoticeGroupObject inNoticeGroup;

    private List<ExcelFeedbackObject> feedbackObjects;

    public List<ExcelFeedbackObject> getFeedbackObjectList() {
        return DataSupport.where("exceltaskobject_id=?",String.valueOf(id)).find(ExcelFeedbackObject.class);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExcelId() {
        return excelId;
    }

    public void setExcelId(String excelId) {
        this.excelId = excelId;
    }

    public String getQuestions() {
        return questions;
    }

    public void setQuestions(String questions) {
        this.questions = questions;
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

    public NoticeGroupObject getInNoticeGroup() {
        return inNoticeGroup;
    }

    public void setInNoticeGroup(NoticeGroupObject inNoticeGroup) {
        this.inNoticeGroup = inNoticeGroup;
    }

    public List<ExcelFeedbackObject> getFeedbackObjects() {
        return feedbackObjects;
    }

    public void setFeedbackObjects(List<ExcelFeedbackObject> feedbackObjects) {
        this.feedbackObjects = feedbackObjects;
    }

}
