package com.flaremars.classmanagers.utils;

import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;

import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * 消息辅助工具类
 */
public enum  MessageUtils {
    INSTANCE;

    public void updateRecentMessage(int targetType,String msg,int id,UserObject targetUser,SubClassObject targetSubClass,String classID,String ownerId) {
        List<UserObject> temp = DataSupport.where("userId=?",ownerId).find(UserObject.class);
        if (temp.size() <= 0) {
            return;
        }
        UserObject mineObject = temp.get(0);
        List<RecentMessageObject> recentMessageObjects = DataSupport.where("targetID=? and type=? and userobject_id=?",
                String.valueOf(id),
                String.valueOf(targetType),
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

        if (recentMessageObjects.size() == 0) {
            RecentMessageObject recentMessageObject = new RecentMessageObject();
            ClassObject targetClass = DataSupport.where("classID=?",classID).find(ClassObject.class).get(0);
            recentMessageObject.setInClass(targetClass);
            recentMessageObject.setNoReadNumber(1);
            recentMessageObject.setContent(content);
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
            recentMessageObject.setOwner(mineObject);
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

    public  void updateRecentMessage(ClassObject classObject,String content,NoticeGroupObject noticeGroupObject,String ownerId) {
        List<UserObject> temp = DataSupport.where("userId=?",ownerId).find(UserObject.class);
        if (temp.size() <= 0) {
            return;
        }
        UserObject mineObject = temp.get(0);
        List<RecentMessageObject> recentMessageObjects = DataSupport.where("targetID=? and type=? and userobject_id=?", String.valueOf(noticeGroupObject.getId()),
                String.valueOf(RecentMessageObject.TYPE_NOTICE_GROUP),String.valueOf(mineObject.getId())).
                find(RecentMessageObject.class);
        if (recentMessageObjects.size() == 0) {
            RecentMessageObject recentMessageObject = new RecentMessageObject();
            recentMessageObject.setInClass(classObject);
            recentMessageObject.setNoReadNumber(0);
            recentMessageObject.setContent(content);
            recentMessageObject.setName(noticeGroupObject.getName());
            recentMessageObject.setTime(new Date());
            recentMessageObject.setType(RecentMessageObject.TYPE_NOTICE_GROUP);
            recentMessageObject.setTargetID(noticeGroupObject.getId());
            recentMessageObject.setOwner(mineObject);
            recentMessageObject.save();
        } else {
            RecentMessageObject recentMessageObject = recentMessageObjects.get(0);
            recentMessageObject.setNoReadNumber(0);
            recentMessageObject.setContent(content);
            recentMessageObject.setName(noticeGroupObject.getName());
            recentMessageObject.setTime(new Date());
            recentMessageObject.update(recentMessageObject.getId());
        }
    }
}
