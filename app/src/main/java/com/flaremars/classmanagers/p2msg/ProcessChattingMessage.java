package com.flaremars.classmanagers.p2msg;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.MessageObject;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.service.CMService;
import com.flaremars.classmanagers.utils.NotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * 处理聊天数据
 */
public class ProcessChattingMessage implements IProcessMsg {
    @Override
    public void process(JSONObject content, Context context, UserObject thisUser) {
        try {
            Log.e("TAG","process" + " " + content);
            if (!content.getString(MessageConst.CONTENT_FROM_ID).equals(thisUser.getUserId())) {

                MessageObject messageObject = new MessageObject();
                messageObject.setContent(content.getString(MessageConst.CONTENT_MSG));
                messageObject.setTime(new Date());
                messageObject.setRead(false);

                List<UserObject> temp = DataSupport.where("userID=?", content.getString(MessageConst.CONTENT_FROM_ID)).
                        find(UserObject.class);
                if (temp.size() == 0) {
                    return;
                }
                UserObject sender = temp.get(0);

                Intent data = new Intent(CMService.HAS_NEW_MESSAGES);
                //!message.getSubClassId().equals("") 意味着是群聊数据
                if (!content.getString(MessageConst.CONTENT_IN_SUB_CLASS).equals("")) {
                    List<SubClassObject> check = DataSupport.where("subClassID=?",content.getString(MessageConst.CONTENT_IN_SUB_CLASS)).
                            find(SubClassObject.class);
                    if (check.size() == 0) {
                        return;
                    }
                    SubClassObject targetSubClass = check.get(0);

                    messageObject.setInSubClassObject(targetSubClass);
                    messageObject.setInUserObject(sender);
                    messageObject.setType(content.getInt(MessageConst.CONTENT_TYPE));
                    data.putExtra("message_sender", targetSubClass.getSubClassID());


                    //记录RecentMessage
                    updateRecentMessage(RecentMessageObject.TYPE_GROUP, content.getString(MessageConst.CONTENT_MSG),
                            targetSubClass.getId(), sender, targetSubClass, content.getString(MessageConst.CONTENT_IN_CLASS),thisUser.getUserId());
                } else {
                    messageObject.setInUserObject(sender);
                    messageObject.setType(content.getInt(MessageConst.CONTENT_TYPE));

                    data.putExtra("message_sender",sender.getUserId());
                    //记录RecentMessage
                    updateRecentMessage(RecentMessageObject.TYPE_PERSON, content.getString(MessageConst.CONTENT_MSG),
                            sender.getId(), sender, null, content.getString(MessageConst.CONTENT_IN_CLASS), thisUser.getUserId());
                }
                messageObject.setOwnerId(thisUser.getUserId());
                messageObject.save();

                data.putExtra("message_id",messageObject.getId());
                context.sendBroadcast(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateRecentMessage(int targetType,String msg,int id,UserObject targetUser,SubClassObject targetSubClass,String classID,String meId) {
        ClassObject targetClass = DataSupport.where("classID=?",classID).find(ClassObject.class).get(0);
        List<UserObject> temp = DataSupport.where("userId=?",meId).find(UserObject.class);
        if (temp.size() <= 0) {
            return;
        }
        UserObject mineObject = temp.get(0);
        List<RecentMessageObject> recentMessageObjects = DataSupport.where("targetID=? and type=? and classobject_id=? and userobject_id=?",
                String.valueOf(id),
                String.valueOf(targetType),
                String.valueOf(targetClass.getId()),
                String.valueOf(mineObject.getId())).
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
        if (targetType == RecentMessageObject.TYPE_GROUP) {
            content = targetUser.getUserRealName() + " : " + content;
        }
        if (recentMessageObjects.size() == 0) {
            RecentMessageObject recentMessageObject = new RecentMessageObject();
            recentMessageObject.setInClass(targetClass);
            recentMessageObject.setNoReadNumber(1);
            recentMessageObject.setContent(content);
            String name;
            if (targetType == RecentMessageObject.TYPE_GROUP) {
                name = targetSubClass.getSubClassName();
            } else {
                name = targetUser.getUserRealName();
                NotificationUtils.INSTANCE.toNotify(targetUser.getUserRealName() + ": " + content);
            }
            recentMessageObject.setName(name);
            recentMessageObject.setTime(new Date());
            recentMessageObject.setImgPath(targetUser.getImgId());
            recentMessageObject.setType(targetType);
            recentMessageObject.setTargetID(id);
            recentMessageObject.setOwner(mineObject);
            recentMessageObject.save();
        } else {
            RecentMessageObject recentMessageObject = recentMessageObjects.get(0);
            int num = recentMessageObject.getNoReadNumber();
            num++;
            recentMessageObject.setNoReadNumber(num);
            recentMessageObject.setTime(new Date());
            recentMessageObject.setContent(content);
            String name;
            if (targetType == RecentMessageObject.TYPE_GROUP) {
                name = targetSubClass.getSubClassName();
            } else {
                name = targetUser.getUserRealName();
                recentMessageObject.setImgPath(targetUser.getImgId());

                if (!MainActivity.BASE_GLOBAL_DATA.getCurClassID().equals("") &&
                        classID.equals(MainActivity.BASE_GLOBAL_DATA.getCurClassID())) {
                    NotificationUtils.INSTANCE.toNotify(targetUser.getUserRealName() + ": " + content);
                }
            }
            recentMessageObject.setName(name);
            recentMessageObject.update(recentMessageObject.getId());
        }
    }
}
