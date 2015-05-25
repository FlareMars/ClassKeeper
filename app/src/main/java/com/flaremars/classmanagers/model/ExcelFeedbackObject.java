package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

/**
 * 信息录制反馈实体类
 */
public class ExcelFeedbackObject extends DataSupport{

    private int id;

    private String whose;

    private String targetPhone;

    private String answer;

    private String whoseName;

    private ExcelTaskObject inExcelTaskObject;

    public String getWhoseName() {
        return whoseName;
    }

    public void setWhoseName(String whoseName) {
        this.whoseName = whoseName;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public ExcelTaskObject getInExcelTaskObject() {
        return inExcelTaskObject;
    }

    public void setInExcelTaskObject(ExcelTaskObject inExcelTaskObject) {
        this.inExcelTaskObject = inExcelTaskObject;
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
}
