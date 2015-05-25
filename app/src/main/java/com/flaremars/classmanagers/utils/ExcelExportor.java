package com.flaremars.classmanagers.utils;

import com.flaremars.classmanagers.model.ExcelTaskObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExcelExportor {

    private static ExcelExportor instance;

    private ExcelExportor() {
    }

    public static ExcelExportor getInstance() {
        if(instance == null){
            synchronized (ExcelExportor.class){
                if(instance == null){
                    instance = new ExcelExportor();
                }
            }
        }
        return instance;
    }

    public String toExcel(JSONArray content,ExcelTaskObject excelTask) {
        try{
            String path = FileUtils.getInstance().getMineFilesDir() + excelTask.getName() +".xls";
            WritableWorkbook writableWorkbook = Workbook.createWorkbook(new File(path));
            WritableSheet sheet1 = writableWorkbook.createSheet("录制结果", 0);
            String[] questions = excelTask.getQuestions().split("_");

            //初始化表头
            Label nameLabel = new Label(0 , 0 , "数据所属对象");
            sheet1.addCell(nameLabel);
            for(int i = 1 ; i <= questions.length ; i++)
            {
                Label label = new Label(i , 0 , questions[i-1]);
                sheet1.addCell(label);
            }

            //填入数据
            for(int j = 1;j <= content.length();j++)
            {
                JSONObject tempObject = content.getJSONObject(j-1);
                String whose = tempObject.getString("whose");
                String answerStr = tempObject.getString("answers");

                //首先填充数据所属对象
                Label ownerLabel = new Label(0 , j , whose);
                sheet1.addCell(ownerLabel);

                String[] answers = answerStr.split("_");
                for(int i = 1 ; i <= answers.length ; i++)
                {
                    Label label = new Label(i , j , answers[i-1]);
                    sheet1.addCell(label);
                }
            }
            writableWorkbook.write();
            writableWorkbook.close();

            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}