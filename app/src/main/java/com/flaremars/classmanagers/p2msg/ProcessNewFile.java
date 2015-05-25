package com.flaremars.classmanagers.p2msg;

import android.content.Context;
import android.content.Intent;

import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.MessageObject;
import com.flaremars.classmanagers.model.MsgType;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.service.CMService;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * 处理新的文件到来
 */
public class ProcessNewFile implements IProcessMsg {
    @Override
    public void process(JSONObject content, Context context, UserObject thisUser) {
        try {
            String targetClass = content.getString(MessageConst.CONTENT_IN_CLASS);
            String fileObjectId = content.getString(MessageConst.CONTENT_TARGET_ID);
            String fileName = content.getString(MessageConst.CONTENT_FILE_NAME);
            String sender = content.getString(MessageConst.CONTENT_FROM);

            ClassObject classObject = DataSupport.where("classID=?", targetClass).find(ClassObject.class).get(0);
            SubClassObject targetSubClass = DataSupport.where("classobject_id=?",String.valueOf(classObject.getId())).
                    find(SubClassObject.class).get(0);
            List<UserObject> temp = DataSupport.where("userId=?", sender).find(UserObject.class);
            if (temp.size() == 0) {
                return;
            }
            UserObject targetUser = temp.get(0);
            //记录MessageObject
            MessageObject messageObject = new MessageObject();
            messageObject.setContent("file:"+fileObjectId+"&"+fileName);
            messageObject.setTime(new Date());
            messageObject.setRead(false);
            messageObject.setInSubClassObject(targetSubClass);
            messageObject.setInUserObject(targetUser);
            if (sender.equals(thisUser.getUserId())) {
                messageObject.setType(MsgType.FILE_MINE.ordinal());
            } else {
                messageObject.setType(MsgType.FILE_OTHERS.ordinal());
            }
            //记录RecentMessage
            updateRecentMessage(RecentMessageObject.TYPE_GROUP, "file:[文件]",
                    targetSubClass.getId(), targetUser, targetSubClass, targetClass,thisUser.getUserId());
            messageObject.setOwnerId(thisUser.getUserId());
            messageObject.save();

            Intent data = new Intent(CMService.HAS_NEW_MESSAGES);
            context.sendBroadcast(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateRecentMessage(int targetType,String msg,int id,UserObject targetUser,SubClassObject targetSubClass,String classID,String ownerId) {

        List<UserObject> temp = DataSupport.where("userId=?",ownerId).find(UserObject.class);
        if (temp.size() <= 0) {
            return;
        }
        UserObject mineObject = temp.get(0);
        List<RecentMessageObject> recentMessageObjects = DataSupport.where("targetID=? and type=? and userobject_id=?", String.valueOf(id),
                String.valueOf(targetType),String.valueOf(mineObject.getId())).
                find(RecentMessageObject.class);

        //content 以“:”分离，前部分为内容的种类，后面才是真正的内容
        String[] partedMsg = msg.split(":");
        String content = "";
        switch (partedMsg[0]) {
            case "normal":
                content = partedMsg[1];
                break;
            case "picture":
                content = "[图片]";
                break;
            case "excel_task":
                content = "[信息录制]";
                break;
            case "file":
                content = "[文件]";
                break;
            default:
        }

        if (recentMessageObjects.size() == 0) {
            RecentMessageObject recentMessageObject = new RecentMessageObject();
            ClassObject targetClass = DataSupport.where("classID=?",classID).find(ClassObject.class).get(0);
            recentMessageObject.setInClass(targetClass);
            recentMessageObject.setNoReadNumber(1);
            recentMessageObject.setContent(content);
            recentMessageObject.setOwner(mineObject);
            String name;
            if (targetType == RecentMessageObject.TYPE_GROUP) {
                name = targetSubClass.getSubClassName();
            } else {
                name = targetUser.getUserRealName();
            }
            recentMessageObject.setName(name);
            recentMessageObject.setTime(new Date());
            recentMessageObject.setImgPath(targetUser.getImgId());
            recentMessageObject.setType(targetType);
            recentMessageObject.setTargetID(id);
            recentMessageObject.save();
        } else {
            RecentMessageObject recentMessageObject = recentMessageObjects.get(0);
            int num = recentMessageObject.getNoReadNumber();
            num++;
            recentMessageObject.setNoReadNumber(num);
            recentMessageObject.setTime(new Date());
            recentMessageObject.setContent(content);
            recentMessageObject.update(recentMessageObject.getId());
        }
    }
}
