package com.flaremars.classmanagers.handler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassMemberObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.ManagerObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.MessageObject;
import com.flaremars.classmanagers.model.MsgType;
import com.flaremars.classmanagers.model.NoticeGroupMemberObject;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.SubClassMemberObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.p2msg.IProcessMsg;
import com.flaremars.classmanagers.p2msg.ProcessQuitGroup;
import com.flaremars.classmanagers.service.CMService;
import com.flaremars.classmanagers.utils.MessageUtils;
import com.flaremars.classmanagers.utils.NormalUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * 处理会话（子群）加入/成员变化事件
 */
public class CMConversationHandler extends AVIMConversationEventHandler {

    private String userId;

    public CMConversationHandler() {
        SharedPreferences sharedPreferences = CMApplication.GLOBAL_CONTEXT.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString(AppConst.USER_ID,"");
    }

    @Override
    public void onMemberLeft(AVIMClient avimClient, AVIMConversation avimConversation, List<String> list, String kickedBy) {

    }

    /*如果是自己加入会话的，list和invitedBy将会是一样的*/
    @Override
    public void onMemberJoined(AVIMClient avimClient, final AVIMConversation avimConversation, final List<String> list, String invitedBy) {
        Log.e("TAG", "onMemberJoined " + list.toString() + " " + invitedBy);
        if (userId.equals("")) {
            return;
        }
        if (!avimClient.getClientId().equals(userId)) {
            return;
        }
        avimConversation.fetchInfoInBackground(new AVIMConversationCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Integer conversationType = (Integer) avimConversation.getAttribute("type");
                    if (conversationType == AppConst.ConversationType_OneOne) {
                        return;
                    }

                    if (list.size() == 1 && list.contains(userId)) {
                        return;
                    }

                    Log.e("TAG", list.toString());
                    //找回发生事件的班级/子群
                    final SubClassObject targetSubClass = DataSupport.where("subClassID=?", avimConversation.getConversationId()).find(SubClassObject.class, true).get(0);
                    final ClassObject targetClass = targetSubClass.getInClass();

                    AVQuery<AVObject> query = new AVQuery<>("CMUserInfo");
                    query.whereContainedIn("userId", list);
//            query.setLimit(list.size());
                    query.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list, AVException e) {
                            if (e == null) {
                                Log.e("UserInfo", list.toString());
                                if (list.size() > 0) {
                                    outer:
                                    for (AVObject userInfo : list) {
                                        String userId = userInfo.getString("userId");
                                        String userName = userInfo.getString("phone");
                                        List<SubClassMemberObject> subClassMemberObjects = targetSubClass.getMemberList();
                                        for (SubClassMemberObject temp : subClassMemberObjects) {
                                            if (temp.getMemberID().equals(userId)) {
                                                continue outer;
                                            }
                                        }

                                        List<UserObject> temps = DataSupport.where("userId=?", userId).find(UserObject.class);
                                        UserObject newUserObject;
                                        if (temps.size() == 0) {
                                            newUserObject = new UserObject();
                                            newUserObject.setImgId(userInfo.getString("headerImg"));
                                            newUserObject.setUserId(userId);
                                            newUserObject.setUserName(userName);
                                            newUserObject.setUserRealName(userInfo.getString("realName"));
                                            newUserObject.save();
                                        } else {
                                            newUserObject = temps.get(0);
                                        }

                                        ClassMemberObject newClassMember = new ClassMemberObject();
                                        newClassMember.setInClass(targetClass);
                                        newClassMember.setMemberName(newUserObject.getUserRealName());
                                        newClassMember.setMemberID(newUserObject.getUserId());
                                        newClassMember.save();

                                        SubClassMemberObject newSubClassMember = new SubClassMemberObject();
                                        newSubClassMember.setInSubClass(targetSubClass);
                                        newSubClassMember.setMemberID(newUserObject.getUserId());
                                        newSubClassMember.setMemberName(newUserObject.getUserName());
                                        newSubClassMember.save();


                                        //如果本用户是这个群的创建者 (或管理员) 自动添加新成员到全员通知组里
                                        List<ManagerObject> managerObjectList = DataSupport.where("classobject_id=?", String.valueOf(targetClass.getId())).find(ManagerObject.class);

                                        if (targetClass.getCreator().equals(CMConversationHandler.this.userId) ||
                                                managerObjectList.size() > 0) {
                                            List<NoticeGroupObject> noticeGroupObjectList =
                                                    DataSupport.where("classobject_id=? and isAllMember = 1", String.valueOf(targetClass.getId())).find(NoticeGroupObject.class);
                                            if (noticeGroupObjectList.size() > 0) {
                                                NoticeGroupMemberObject newNoticeGroupMember = new NoticeGroupMemberObject();
                                                newNoticeGroupMember.setInNoticeGroup(noticeGroupObjectList.get(0));
                                                newNoticeGroupMember.setMemberID(newUserObject.getUserId());
                                                newNoticeGroupMember.setMemberName(newUserObject.getUserRealName());
                                                newNoticeGroupMember.save();
                                            }
                                        }

                                        int peopleCount = targetClass.getTotalPeopleCount();
                                        peopleCount++;
                                        targetClass.setTotalPeopleCount(peopleCount);
                                        targetClass.update(targetClass.getId());

                                        //创建一条提示有新成员到来的本地消息
                                        MessageObject messageObject = new MessageObject();
                                        messageObject.setContent("normal:" + "大家好，我是" + newUserObject.getUserRealName() + ",我来啦~");
                                        messageObject.setTime(new Date());
                                        messageObject.setRead(false);
                                        messageObject.setInSubClassObject(targetSubClass);
                                        messageObject.setInUserObject(newUserObject);
                                        messageObject.setType(MsgType.NORMAL_OTHERS.ordinal());
                                        messageObject.setOwnerId(CMConversationHandler.this.userId);
                                        messageObject.save();

                                        //记录RecentMessage
                                        MessageUtils.INSTANCE.updateRecentMessage(RecentMessageObject.TYPE_GROUP, "normal:" + "大家好，我是" + newUserObject.getUserRealName() + ",我来啦~",
                                                targetSubClass.getId(), newUserObject, targetSubClass, targetClass.getClassID(),CMConversationHandler.this.userId);

                                        Intent data = new Intent(CMService.HAS_NEW_MESSAGES);
                                        CMApplication.GLOBAL_CONTEXT.sendBroadcast(data);
                                    }
                                }
                            } else {
                                NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT, e);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onKicked(AVIMClient avimClient, AVIMConversation avimConversation, String kickedBy) {
        IProcessMsg quitGroup = new ProcessQuitGroup();
        JSONObject content = new JSONObject();
        try {
            content.put(MessageConst.CONTENT_TARGET_ID,avimConversation.getConversationId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        UserObject temp = new UserObject();
        temp.setUserId(userId);
        quitGroup.process(content,CMApplication.GLOBAL_CONTEXT,temp);
    }

    @Override
    public void onInvited(AVIMClient avimClient, final AVIMConversation avimConversation, final String operator) {
        if (userId.equals("")) {
            return;
        }
        if (!avimClient.getClientId().equals(MainActivity.BASE_GLOBAL_DATA.getUserID())) {
            return;
        }
        avimConversation.fetchInfoInBackground(new AVIMConversationCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    final String targetId = avimConversation.getConversationId();
                    Log.e("TAG","onInvited " + targetId);
                    if ((Integer)avimConversation.getAttribute("type") == AppConst.ConversationType_OneOne) {
                        UserObject targetUserObject = DataSupport.where("userId=?",operator).find(UserObject.class).get(0);
                        if (targetUserObject.getConversationId() == null || targetUserObject.getConversationId().equals("")) {
                            targetUserObject.setConversationId(avimConversation.getConversationId());
                            targetUserObject.update(targetUserObject.getId());
                        }
                        return;
                    }
                    String classId = (String)avimConversation.getAttribute("classId");
                    ClassObject classObject = DataSupport.where("classID=?",classId).find(ClassObject.class).get(0);

                    if (operator.equals(userId)) {
                        return;
                    }
                    List<SubClassObject> check = DataSupport.where("subClassID=?", avimConversation.getConversationId()).find(SubClassObject.class);
                    SubClassObject newSubClass = new SubClassObject();
                    if (check.size() > 0) {
                        return;
                    } else {
                        newSubClass.setInClass(classObject);
                        newSubClass.setTime(new Date());
                        newSubClass.setSubClassID(avimConversation.getConversationId());
                        newSubClass.setSubClassName(avimConversation.getName());
                        newSubClass.save();
                    }

                    List<String> memberIds = avimConversation.getMembers();
                    for (String id : memberIds) {
                        UserObject userObject = DataSupport.where("userID=?",id).find(UserObject.class).get(0);
                        SubClassMemberObject newMember = new SubClassMemberObject();
                        newMember.setMemberID(userObject.getUserId());
                        newMember.setMemberName(userObject.getUserRealName());
                        newMember.setInSubClass(newSubClass);
                        newMember.save();
                    }

                    AVQuery<AVUser> query = AVUser.getQuery();
                    query.getInBackground(userId, new GetCallback<AVUser>() {
                        @Override
                        public void done(AVUser object, AVException e) {
                            if (e == null) {
                                object.add("subClassIds", targetId);
                                object.saveInBackground();
                            } else {
                                NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT,e);
                            }
                        }
                    });
                }
            }
        });

    }

}
