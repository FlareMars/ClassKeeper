package com.flaremars.classmanagers.p2msg;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.ExcelFeedbackObject;
import com.flaremars.classmanagers.model.ExcelTaskObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.RecentNoticeGroupMsg;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.service.CMService;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * 处理消息录制任务
 */
public class ProcessExcelFeedback implements IProcessMsg {
    @Override
    public void process(JSONObject content, Context context, UserObject thisUser) {
        try {
            List<ExcelTaskObject> taskObjects = DataSupport.where("excelId=?", content.getString(MessageConst.CONTENT_TARGET_ID)).
                    find(ExcelTaskObject.class);
            ExcelTaskObject taskObject;
            if (taskObjects.size() > 0) {
                taskObject = taskObjects.get(0);
            } else {
                return;
            }
            ExcelFeedbackObject tempFeedback = DataSupport.where("exceltaskobject_id=? and whose=?",
                    String.valueOf(taskObject.getId()),content.getString(MessageConst.CONTENT_FROM_ID)).find(ExcelFeedbackObject.class).get(0);
            tempFeedback.setAnswer(content.getString(MessageConst.CONTENT_EXCEL_ANSWERS));
            tempFeedback.update(tempFeedback.getId());
            taskObject.setNewFeedback(true);
            taskObject.update(taskObject.getId());

            //添加通知组反馈
            Cursor cursor = DataSupport.findBySQL("select noticegroupobject_id from exceltaskobject where id=?",String.valueOf(taskObject.getId()));
            long targetId = 0;
            if (cursor != null && cursor.moveToFirst()) {
                targetId = cursor.getLong(0);
            }
            NoticeGroupObject noticeGroupObject = DataSupport.find(NoticeGroupObject.class, targetId);

            assert noticeGroupObject != null;
            List<RecentNoticeGroupMsg> recentNoticeGroupMsgs = DataSupport.where("noticegroupobject_id=?",
                    String.valueOf(noticeGroupObject.getId())).find(RecentNoticeGroupMsg.class);
            if (recentNoticeGroupMsgs.size() == 0) {
                RecentNoticeGroupMsg recentNoticeGroupMsg = new RecentNoticeGroupMsg();
                recentNoticeGroupMsg.setContent(taskObject.getName());
                recentNoticeGroupMsg.setTime(new Date());
                recentNoticeGroupMsg.setNoticeGroup(noticeGroupObject);
                recentNoticeGroupMsg.setNoReadNumber(0);
                recentNoticeGroupMsg.save();
            } else {
                RecentNoticeGroupMsg recentNoticeGroupMsg = recentNoticeGroupMsgs.get(0);
                recentNoticeGroupMsg.setContent(taskObject.getName());
                recentNoticeGroupMsg.setTime(new Date());
                recentNoticeGroupMsg.setNoReadNumber(0);
                recentNoticeGroupMsg.update(recentNoticeGroupMsg.getId());
            }


            //更新最近消息列表的“未读反馈”
            List<UserObject> temp = DataSupport.where("userId=?", thisUser.getUserId()).find(UserObject.class);
            if (temp.size() <= 0) {
                return;
            }
            UserObject mineObject = temp.get(0);
            ClassObject classObject = DataSupport.where("classID=?",content.getString(MessageConst.CONTENT_IN_CLASS)).
                    find(ClassObject.class).get(0);
            List<RecentMessageObject> recentMessageObjects = DataSupport.where("type=0 and targetId=? and classobject_id=? and userobject_id=?",
                    String.valueOf(noticeGroupObject.getId()),String.valueOf(classObject.getId()),
                    String.valueOf(mineObject.getId())).
                    find(RecentMessageObject.class);

            if (recentMessageObjects.size() == 0) {
                RecentMessageObject recentMessageObject = new RecentMessageObject();
                recentMessageObject.setInClass(classObject);
                recentMessageObject.setNoReadNumber(1);
                recentMessageObject.setContent(taskObject.getName());
                recentMessageObject.setName(noticeGroupObject.getName());
                recentMessageObject.setTime(new Date());
                recentMessageObject.setImgPath("");
                recentMessageObject.setType(RecentMessageObject.TYPE_NOTICE_GROUP);
                recentMessageObject.setTargetID(noticeGroupObject.getId());
                recentMessageObject.setOwner(mineObject);
                recentMessageObject.save();
            } else {
                RecentMessageObject recentMessageObject = recentMessageObjects.get(0);
                int num = recentMessageObject.getNoReadNumber();
                num++;
                recentMessageObject.setNoReadNumber(num);
                recentMessageObject.setTime(new Date());
                recentMessageObject.update(recentMessageObject.getId());
            }

//            NotificationUtils.INSTANCE.toNotify("有新的信息录制反馈");

            Intent data = new Intent(CMService.HAS_NEW_TASK);
            context.sendBroadcast(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
