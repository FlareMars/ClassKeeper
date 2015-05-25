package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

/**
 * 表格录制模板实体类
 */
public class ExcelModelObject extends DataSupport {
    private int id;

    private String name;

    private String questions;

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
}
