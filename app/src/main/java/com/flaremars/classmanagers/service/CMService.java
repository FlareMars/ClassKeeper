package com.flaremars.classmanagers.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassMemberObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.p2msg.IProcessMsg;
import com.flaremars.classmanagers.p2msg.ProcessChattingMessage;
import com.flaremars.classmanagers.p2msg.ProcessExcelFeedback;
import com.flaremars.classmanagers.p2msg.ProcessFileFromComputer;
import com.flaremars.classmanagers.p2msg.ProcessNewExcel;
import com.flaremars.classmanagers.p2msg.ProcessNewFile;
import com.flaremars.classmanagers.p2msg.ProcessNewNotice;
import com.flaremars.classmanagers.p2msg.ProcessNoticeFeedback;
import com.flaremars.classmanagers.p2msg.ProcessQuitGroup;
import com.flaremars.classmanagers.p2msg.ProcessRemoveManager;
import com.flaremars.classmanagers.p2msg.ProcessToBeManager;
import com.flaremars.classmanagers.utils.FileUtils;
import com.flaremars.classmanagers.utils.NormalUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 配合LeanCloud的服务
 */
public class CMService extends Service{

    public static final String HAS_NEW_MESSAGES = "HAS_NEW_MESSAGE";
    //通知，消息录制等等
    public static final String HAS_NEW_TASK = "HAS_NEW_TASK";

    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);

    private static UserObject userObjectForMe;

    private static final SparseArray<String> PROCESS_MSG_CLASS_NAMES = new SparseArray<>();
    static {
        PROCESS_MSG_CLASS_NAMES.put(1, ProcessRemoveManager.class.getName());
        PROCESS_MSG_CLASS_NAMES.put(2, ProcessToBeManager.class.getName());
        PROCESS_MSG_CLASS_NAMES.put(4, ProcessNewFile.class.getName());
        PROCESS_MSG_CLASS_NAMES.put(6, ProcessNewNotice.class.getName());
        PROCESS_MSG_CLASS_NAMES.put(7, ProcessNoticeFeedback.class.getName());
        PROCESS_MSG_CLASS_NAMES.put(8, ProcessNewExcel.class.getName());
        PROCESS_MSG_CLASS_NAMES.put(9, ProcessExcelFeedback.class.getName());
        PROCESS_MSG_CLASS_NAMES.put(10, ProcessChattingMessage.class.getName());
        PROCESS_MSG_CLASS_NAMES.put(11, ProcessQuitGroup.class.getName());
        PROCESS_MSG_CLASS_NAMES.put(12, ProcessFileFromComputer.class.getName());
    }

    //获取班级的最新头像，所在子群的名字，联系人的头像
    private Runnable getNewestData = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sharedPreferences = getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,MODE_PRIVATE);
            long lastUpdateTime = sharedPreferences.getLong("last_update_data_time",0);

            if (!NormalUtils.INSTANCE.isNetworkRegularWork(CMService.this)) {
                NormalUtils.INSTANCE.showToast(CMService.this,"网络状态不佳，请检查网络");
                return;
            }
            final boolean[] isNetworkWorking = {true};

            //班级id，联系人id，子群id缓存
            List<String> classIds = new ArrayList<>();
            final Set<String> contactIds = new HashSet<>();
            List<String> subClassIds = new ArrayList<>();

            //填充三大类id
            List<ClassObject> classObjectList = DataSupport.findAll(ClassObject.class);
            for (final ClassObject tempObject : classObjectList) {
                classIds.add(tempObject.getClassID());

                List<ClassMemberObject> thisClassMembers = tempObject.getMemberList();
                for (final ClassMemberObject tempMember : thisClassMembers) {
                    contactIds.add(tempMember.getMemberID());

                }
            }

            if (classIds.size() == 0) {
                return;
            }

            List<SubClassObject> subClassObjects = DataSupport.findAll(SubClassObject.class);
            for (final SubClassObject tempObject : subClassObjects) {
                subClassIds.add(tempObject.getSubClassID());
            }

            //id整理完毕，以下进行批量查找
            AVQuery<AVObject> classQuery = new AVQuery<>("CMClassObject");
            classQuery.whereContainedIn("objectId", classIds);
//            classQuery.whereGreaterThanOrEqualTo("updatedAt", new Date(lastUpdateTime));
            classQuery.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    if (e == null) {
                        for (AVObject temp : list) {
                            final ClassObject tempObject = DataSupport.where("classID=?", temp.getObjectId()).find(ClassObject.class).get(0);
                            tempObject.setHeaderPath(temp.getString("headerImg"));
                            tempObject.update(tempObject.getId());

                            if (temp.getInt("studentCount") > tempObject.getTotalPeopleCount()) {
                                //如果本地人员数据少于网络数据的话，重新获取缺少的那部分数据
                                //但是这种情况很少有
                                AVIMConversationQuery query1 = CMApplication.imClient.getQuery();
                                query1.whereEqualTo("objectId", temp.getString("allMembersConversation"));
                                query1.findInBackground(new AVIMConversationQueryCallback() {
                                    @Override
                                    public void done(List<AVIMConversation> list, AVException e) {
                                        if (e == null) {
                                            if (list.size() > 0) {
                                                AVIMConversation conversation = list.get(0);
                                                List<String> members = conversation.getMembers();

                                                List<ClassMemberObject> classCurMembers = tempObject.getMemberList();
                                                List<String> memberIds = new ArrayList<>();
                                                for (ClassMemberObject memberObject : classCurMembers) {
                                                    memberIds.add(memberObject.getMemberID());
                                                }
                                                List<String> lostMembers = new ArrayList<>();
                                                for (String check : members) {
                                                    if (!memberIds.contains(check)) {
                                                        lostMembers.add(check);
                                                    }
                                                }

                                                if (members.size() > 0) {
                                                    AVQuery<AVObject> userInfoQuery = new AVQuery<>("CMUserInfo");
                                                    userInfoQuery.whereContainedIn("userId", lostMembers);
                                                    userInfoQuery.findInBackground(new FindCallback<AVObject>() {
                                                        @Override
                                                        public void done(List<AVObject> list, AVException e) {
                                                            if (e == null) {
                                                                if (list.size() > 0) {
                                                                    tempObject.setTotalPeopleCount(tempObject.getTotalPeopleCount()+list.size());
                                                                    tempObject.update(tempObject.getId());
                                                                    for (AVObject userInfo : list) {
                                                                        List<UserObject> temp = DataSupport.where("userId=?",userInfo.getString("userId")).
                                                                                find(UserObject.class);
                                                                        UserObject targetUser;
                                                                        if (temp.size() > 0) {
                                                                            targetUser = temp.get(0);
                                                                        } else {
                                                                            targetUser = new UserObject();
                                                                            targetUser.setImgId(userInfo.getString("headerImg"));
                                                                            targetUser.setUserRealName(userInfo.getString("realName"));
                                                                            targetUser.setUserId(userInfo.getString("userId"));
                                                                            targetUser.setUserName(userInfo.getString("phone"));
                                                                            targetUser.save();
                                                                        }

                                                                        //TODO 考虑使用CMConversationHandler的onMemberJoined
                                                                        ClassMemberObject newMember = new ClassMemberObject();
                                                                        newMember.setInClass(tempObject);
                                                                        newMember.setMemberName(targetUser.getUserRealName());
                                                                        newMember.setMemberID(targetUser.getUserId());
                                                                        newMember.save();
                                                                    }
                                                                }
                                                            } else {
                                                                isNetworkWorking[0] = false;
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        } else {
                                            Log.e("TAG",e.getMessage());
                                            isNetworkWorking[0] = false;
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        isNetworkWorking[0] = false;
                    }
                }
            });

            AVQuery<AVObject> userInfoQuery = new AVQuery<>("CMUserInfo");
            userInfoQuery.whereContainedIn("userId", contactIds);
            userInfoQuery.whereGreaterThanOrEqualTo("updatedAt", new Date(lastUpdateTime));
            userInfoQuery.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    if (e == null) {
                        for (AVObject temp : list) {
                            UserObject targetUser = DataSupport.where("userId=?", temp.getString("userId")).find(UserObject.class).get(0);
                            targetUser.setImgId(temp.getString("headerImg"));
                            targetUser.setUserName(temp.getString("phone"));
                            targetUser.update(targetUser.getId());
                        }
                    } else {
                        isNetworkWorking[0] = false;
                    }
                }
            });

            AVIMConversationQuery query1 = CMApplication.imClient.getQuery();
            query1.whereContainsIn("conversationId",subClassIds);
            query1.whereGreaterThanOrEqualsTo("updatedAt", new Date(lastUpdateTime));
            query1.findInBackground(new AVIMConversationQueryCallback() {
                @Override
                public void done(List<AVIMConversation> list, AVException e) {
                    if (e == null) {
                        for (AVIMConversation temp : list) {
                            SubClassObject targetSubClass = DataSupport.where("subClassID=?", temp.getConversationId()).find(SubClassObject.class).get(0);
                            targetSubClass.setSubClassName(temp.getName());
                            targetSubClass.update(targetSubClass.getId());
                        }
                    } else {
                        isNetworkWorking[0] = false;
                    }
                }
            });

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("last_update_data_time",new Date().getTime());
            editor.apply();

            //让线程暂停5000秒，目的在于让isNetworkWorking 充分赋值
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!isNetworkWorking[0]) {
                NormalUtils.INSTANCE.showToast(CMService.this,"网络状态不佳，请检查网络");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,MODE_PRIVATE);
        List<UserObject> objects = DataSupport.where("userId=?", preferences.getString(AppConst.USER_ID, "")).find(UserObject.class);
        if (objects.size() > 0) {
            userObjectForMe = objects.get(0);
        } else {
            Log.e("TAG","不知名错误,本地用户获取失败~");
            userObjectForMe = null;
            stopSelf();
            return;
        }

        scheduledThreadPool.scheduleAtFixedRate(getNewestData, 0, 2, TimeUnit.MINUTES);

        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                FileUtils.getInstance().listAllFile();
            }
        }, 0, 5, TimeUnit.MINUTES);

        //定时监控IMClient的打开状态
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!CMApplication.isClientOpened) {
                    if (userObjectForMe == null) {
                        SharedPreferences preferences = getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,MODE_PRIVATE);
                        List<UserObject> objects = DataSupport.where("userId=?", preferences.getString(AppConst.USER_ID, "")).find(UserObject.class);
                        if (objects.size() > 0) {
                            userObjectForMe = objects.get(0);
                        } else {
                            userObjectForMe = new UserObject();
                            userObjectForMe.setUserId(preferences.getString(AppConst.USER_ID, ""));
                        }
                    }
                    CMApplication.openIMClient(userObjectForMe.getUserId());
                }
            }
        },40,60,TimeUnit.SECONDS);
//        messageReceiver = new CMMessageReceiver();
//        IntentFilter filter = new IntentFilter("cm.action.MESSAGE");
//        registerReceiver(messageReceiver,filter);
    }

    public static class CMMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("com.avos.avoscloud.Data");
            Log.e("CMMessageReceiver","onReceive : " + message);
            processMessage(message);
        }
    }

    private static void processMessage(String message) {
        try {
            if (message == null) {
                return;
            }

            JSONObject jsonObject = new JSONObject(message);
            int type = jsonObject.getInt(MessageConst.MSG_TYPE);
            try {
                IProcessMsg instance = (IProcessMsg)Class.forName(PROCESS_MSG_CLASS_NAMES.get(type)).newInstance();
                instance.process(jsonObject,CMApplication.GLOBAL_CONTEXT,userObjectForMe);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            SharedPreferences sharedPreferences = CMApplication.GLOBAL_CONTEXT.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("last_stop_service_time", new Date().getTime());
            editor.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("TAG", "CMService - onStartCommand");

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("TAG","onDestroy");
        scheduledThreadPool.shutdown();
        SharedPreferences sharedPreferences = getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("last_stop_service_time", new Date().getTime());
        editor.apply();
//        if (messageReceiver != null) {
//            unregisterReceiver(messageReceiver);
//        }
        if (CMApplication.imClient != null) {
            CMApplication.imClient.close(new AVIMClientCallback() {
                @Override
                public void done(AVIMClient avimClient, AVException e) {
                    if (e != null) {
                        Log.e("TAG",e.getMessage());
                    } else {
                        CMApplication.isClientOpened = false;
                        Log.e("TAG","client close success~");
                    }
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
