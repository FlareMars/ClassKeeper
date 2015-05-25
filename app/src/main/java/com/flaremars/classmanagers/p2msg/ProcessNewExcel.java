package com.flaremars.classmanagers.p2msg;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.ExcelObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.service.CMService;
import com.flaremars.classmanagers.utils.NotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * 处理消息录制任务
 */
public class ProcessNewExcel implements IProcessMsg {
    @Override
    public void process(JSONObject content, Context context, UserObject thisUser) {
        try {
            Log.e("TAG", "handleNewExcel");
            ClassObject classObject = DataSupport.where("classID=?", content.getString(MessageConst.CONTENT_IN_CLASS)).
                    find(ClassObject.class).get(0);

            ExcelObject newExcelTask = new ExcelObject();
            newExcelTask.setAnswers("");
            newExcelTask.setTime(new Date(content.getLong(MessageConst.CONTENT_TIME)));
            newExcelTask.setName(content.getString(MessageConst.CONTENT_EXCEL_NAME));
            newExcelTask.setExcelId(content.getString(MessageConst.CONTENT_TARGET_ID));
            newExcelTask.setQuestions(content.getString(MessageConst.CONTENT_EXCEL_CONTENT));
            newExcelTask.setNameOfSender(content.getString(MessageConst.CONTENT_FROM));
            newExcelTask.setIdOfSender(content.getString(MessageConst.CONTENT_FROM_ID));
            newExcelTask.setInClassId(classObject.getClassID());
            newExcelTask.save();

            List<UserObject> temp = DataSupport.where("userId=?", thisUser.getUserId()).find(UserObject.class);
            if (temp.size() <= 0) {
                return;
            }
            UserObject mineObject = temp.get(0);
            RecentMessageObject recentMessageObject = new RecentMessageObject();
            recentMessageObject.setInClass(classObject);
            recentMessageObject.setNoReadNumber(1);
            recentMessageObject.setContent("");
            recentMessageObject.setName(newExcelTask.getName());
            recentMessageObject.setTime(newExcelTask.getTime());
            recentMessageObject.setImgPath("");
            recentMessageObject.setType(RecentMessageObject.TYPE_EXCEL);
            recentMessageObject.setTargetID(newExcelTask.getId());
            recentMessageObject.setOwner(mineObject);
            recentMessageObject.save();


            NotificationUtils.INSTANCE.toNotify("信息录制 : " + newExcelTask.getName());

            Intent data = new Intent(CMService.HAS_NEW_TASK);
            context.sendBroadcast(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
