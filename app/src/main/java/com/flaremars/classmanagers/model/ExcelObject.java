package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 *  消息录制实体类
 */
public class ExcelObject extends DataSupport{

    private int id;

    private String name;

    private String questions;

    private String answers;

    private String nameOfSender;

    private String idOfSender;

    private Date time;

    private String excelId;

    private String inClassId;

    public String getInClassId() {
        return inClassId;
    }

    public void setInClassId(String inClassId) {
        this.inClassId = inClassId;
    }

    public String getIdOfSender() {
        return idOfSender;
    }

    public void setIdOfSender(String idOfSender) {
        this.idOfSender = idOfSender;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestions() {
        return questions;
    }

    public void setQuestions(String questions) {
        this.questions = questions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public String getNameOfSender() {
        return nameOfSender;
    }

    public void setNameOfSender(String nameOfSender) {
        this.nameOfSender = nameOfSender;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getExcelId() {
        return excelId;
    }

    public void setExcelId(String excelId) {
        this.excelId = excelId;
    }
}
