package com.flaremars.classmanagers.p2msg;

import android.content.Context;
import android.util.Log;

import com.flaremars.classmanagers.model.ClassMemberObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.ManagerObject;
import com.flaremars.classmanagers.model.NoticeGroupMemberObject;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.model.MessageConst;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 处理被邀请成为管理员
 */
public class ProcessToBeManager implements IProcessMsg {
    @Override
    public void process(JSONObject content, Context context, UserObject thisUser) {
        try {
            ClassObject target = DataSupport.where("classID=?", content.getString(MessageConst.CONTENT_IN_CLASS)).
                    find(ClassObject.class).get(0);
            Log.e("TAG","toBeManager");
            ManagerObject newManager = new ManagerObject();
            newManager.setInClass(target);
            newManager.setName(thisUser.getUserRealName());
            newManager.setManagerID(thisUser.getUserId());
            newManager.save();

            //创建一个全员通知组
            List<NoticeGroupObject> allMemberNoticeGroup = DataSupport.where("classobject_id=? and isAllMember = 1",
                    String.valueOf(target.getId())).find(NoticeGroupObject.class);

            if (allMemberNoticeGroup.size() == 0) {
                NoticeGroupObject targetNoticeGroup = new NoticeGroupObject();
                targetNoticeGroup.setInClass(target);
                targetNoticeGroup.setNoticeGroupID(new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date()));
                targetNoticeGroup.setName("[" + target.getName() + "] 全体成员通知组");
                targetNoticeGroup.setAllMember(true);
                targetNoticeGroup.save();

                List<ClassMemberObject> memberObjects = target.getMemberList();
                for (ClassMemberObject member : memberObjects) {
                    NoticeGroupMemberObject newMember = new NoticeGroupMemberObject();
                    newMember.setMemberName(member.getMemberName());
                    newMember.setMemberID(member.getMemberID());
                    newMember.setInNoticeGroup(targetNoticeGroup);
                    newMember.save();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
