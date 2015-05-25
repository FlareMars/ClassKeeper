package com.flaremars.classmanagers.p2msg;

import android.content.Context;
import android.content.Intent;

import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.NoticeObject;
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
 * 处理到来的通知消息
 */
public class ProcessNewNotice implements IProcessMsg {
    @Override
    public void process(JSONObject content, Context context, UserObject thisUser) {
        try {

            NoticeObject newNoticeObject = new NoticeObject();
            newNoticeObject.setRead(false);
            newNoticeObject.setNameOfSender(content.getString(MessageConst.CONTENT_FROM));
            newNoticeObject.setName(content.getString(MessageConst.CONTENT_NOTICE_NAME));
            newNoticeObject.setContent(content.getString(MessageConst.CONTENT_NOTICE_CONTENT));
            newNoticeObject.setNoticeId(content.getString(MessageConst.CONTENT_TARGET_ID));
            newNoticeObject.setTime(new Date(content.getLong(MessageConst.CONTENT_TIME)));
            newNoticeObject.setIdOfSender(content.getString(MessageConst.CONTENT_FROM_ID));
            newNoticeObject.save();

            ClassObject classObject = DataSupport.where("classID=?", content.getString(MessageConst.CONTENT_IN_CLASS)).find(ClassObject.class).get(0);
            List<UserObject> temp = DataSupport.where("userId=?", thisUser.getUserId()).find(UserObject.class);
            if (temp.size() <= 0) {
                return;
            }
            UserObject mineObject = temp.get(0);
            RecentMessageObject recentMessageObject = new RecentMessageObject();
            recentMessageObject.setInClass(classObject);
            recentMessageObject.setNoReadNumber(1);
            recentMessageObject.setContent("");
            recentMessageObject.setName(content.getString(MessageConst.CONTENT_NOTICE_NAME));
            recentMessageObject.setTime(new Date());
            recentMessageObject.setImgPath("");
            recentMessageObject.setType(RecentMessageObject.TYPE_NOTICE);
            recentMessageObject.setTargetID(newNoticeObject.getId());
            recentMessageObject.setOwner(mineObject);
            recentMessageObject.save();

            NotificationUtils.INSTANCE.toNotify("通知 : " + newNoticeObject.getContent());

            Intent data = new Intent(CMService.HAS_NEW_TASK);
            context.sendBroadcast(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
