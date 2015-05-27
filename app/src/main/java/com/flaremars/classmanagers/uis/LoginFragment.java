package com.flaremars.classmanagers.uis;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.RequestPasswordResetCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.BeforeMainActivity;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassMemberObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.SubClassMemberObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.model.UserPersonalInfo;
import com.flaremars.classmanagers.utils.BitmapUtils;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.MyRippleView;
import com.flaremars.classmanagers.views.ProgressDialog;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoginFragment extends Fragment {

    private EditText usernameEt;

    private EditText passwordEt;

    private MyRippleView loginBtn;

    private BeforeMainActivity context;

    private SharedPreferences preferences;
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = (BeforeMainActivity)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = context.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Activity.MODE_PRIVATE);

        //TODO 获取本地用户缓存
        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            String targetUserId = preferences.getString(AppConst.USER_ID,"");

            CMApplication.openIMClient(targetUserId);

            if (targetUserId == null || targetUserId.equals("")) {
                return;
            }
            MainActivity.BASE_GLOBAL_DATA.setUserID(targetUserId);

            String curClassId = preferences.getString(AppConst.CUR_CLASS_ID,"");
            if (curClassId == null || curClassId.equals("")) {
                context.setCurFragment(BeforeMainActivity.FRAGMENT_GUIDE_PAGE);
                FragmentManager manager = context.getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out,R.anim.push_left_in, R.anim.push_left_out);
                transaction.replace(BeforeMainActivity.CONTAINER_ID, new GuidePageFragment());
                transaction.addToBackStack(null);
                transaction.commit();

                AVInstallation installation = AVInstallation.getCurrentInstallation();
                installation.put("userId", targetUserId);
                installation.saveInBackground(new SaveCallback() {
                    public void done(AVException e) {
                        if (e == null) {
                            Log.e("TAG", "储存设备表成功--");
                        } else {
                            Log.e("TAG", "储存设备表失败--" + e.getCode());
                        }
                    }
                });

                //初始化单聊相关数据
                ContentValues cv = new ContentValues();
                cv.put("conversationId","");
                DataSupport.updateAll(UserObject.class, cv, "userId <> ?", targetUserId);
            } else {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                context.finish();
            }
        }
    }

    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        BitmapUtils.INSTANCE.initBitmapUtils(context);

        usernameEt = (EditText) view.findViewById(R.id.et_login_username);
        passwordEt = (EditText) view.findViewById(R.id.et_login_password);
        loginBtn = (MyRippleView) view.findViewById(R.id.mrv_login);
        TextView forgetPasswordBtn = (TextView) view.findViewById(R.id.tv_btn_login_forget_password);
        TextView toRegisterBtn = (TextView) view.findViewById(R.id.tv_btn_login_to_register);

        //显示已有数据
//        int startUpCount = preferences.getInt("start_up_count", 0);
        String curUserName = preferences.getString(AppConst.USER_NAME,"");
        String curUserPassword = preferences.getString(AppConst.USER_PASSWORD,"");

        usernameEt.setText(curUserName);
        passwordEt.setText(curUserPassword);

        view.findViewById(R.id.tv_btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginBtn.animateRipple();
            }
        });

        loginBtn.setOnAnimationEndListener(new MyRippleView.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                //TODO 登陆实现
                final String username = usernameEt.getText().toString();
                final String password = passwordEt.getText().toString();

                if (username.equals("") || password.equals("")) {
                    NormalUtils.INSTANCE.showToast(context, "用户名/密码不能为空");
                    return;
                }
                progressDialog = new ProgressDialog(context, "正在登陆");
                progressDialog.show();

                AVUser.logInInBackground(username, password, new LogInCallback<AVUser>() {
                    public void done(final AVUser user, AVException e) {

                        //保证CMApplication.imClient在此界面下是关闭状态的,尽管可能本身就是关闭状态
                        if (user != null) {
                            String curUser = preferences.getString(AppConst.USER_ID,"");
                            if (curUser != null && !curUser.equals(user.getObjectId())) {
                                //TODO 清空
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear().apply();
                            }
                            if (CMApplication.isClientOpened) {
                                CMApplication.imClient.close(new AVIMClientCallback() {
                                    @Override
                                    public void done(AVIMClient avimClient, AVException e) {
                                        if (e == null) {
                                            initToLogin(user,username,password);
                                        }
                                    }
                                });
                            } else {
                                initToLogin(user,username,password);
                            }
                        } else {
                            progressDialog.dismiss();
                            if (e.getCode() == 210) {
                                NormalUtils.INSTANCE.showToast(context,"用户名/密码错误");
                            } else {
                                NormalUtils.INSTANCE.showError(context, e.getCode(), e.getMessage());
                            }
                        }
                    }
                });
            }
        });


        forgetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,ResetPasswordActivity.class);
                startActivity(intent);
                usernameEt.setText("");
                passwordEt.setText("");
            }
        });

        toRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = context.getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out,R.anim.push_left_in, R.anim.push_left_out);
                transaction.replace(BeforeMainActivity.CONTAINER_ID, new RegisterOneFragment());
                transaction.commit();
                context.setCurFragment(BeforeMainActivity.FRAGMENT_REGISTER_ONE);
            }
        });
        return view;
    }

    private void initToLogin(final AVUser user, final String username, final String password) {
        CMApplication.imClient = AVIMClient.getInstance(user.getObjectId());
        CMApplication.imClient.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVException e) {
                if (e != null) {
                    Log.e("TAG", e.getMessage());
                } else {
                    //如果本地存在相应用户的本地数据
                    List<UserObject> targetUserObjects = DataSupport.where("userName=?", username).find(UserObject.class);
                    if (targetUserObjects.size() > 0) {
                        progressDialog.dismiss();
                        readyToLogin(username, password);
                    } else {
                        //TODO 进行用户本地化
                        //首先清空ClassObject以及UserObject数据库，以免出现混乱
//                                            DataSupport.deleteAll(ClassObject.class);
//                                            DataSupport.deleteAll(UserObject.class);

                        //获取所有的所在班级
                        user.getRelation("inClasses").getQuery().findInBackground(new FindCallback<AVObject>() {
                            @Override
                            public void done(List<AVObject> list, AVException e) {
                                if (e != null) {
                                    NormalUtils.INSTANCE.showError(context, e);
                                } else {
                                    //进行联系人和班级的本地化
                                    Log.e("用户数据本地化","成功获取班级列表:" + list.size() + "");
                                    List<String> allMainConversation = new ArrayList<>();
                                    final Map<String,AVObject> conversationToClass = new HashMap<>();

                                    for (final AVObject cmClassObject : list) {
                                        allMainConversation.add(cmClassObject.getString("allMembersConversation"));
                                        conversationToClass.put(cmClassObject.getString("allMembersConversation"),cmClassObject);
                                    }
                                    AVIMConversationQuery query1 = CMApplication.imClient.getQuery();
                                    query1.whereContainsIn("objectId", allMainConversation);
                                    query1.findInBackground(new AVIMConversationQueryCallback() {
                                        @Override
                                        public void done(final List<AVIMConversation> conversationList, AVException e) {
                                            if (e == null) {
                                                if (conversationList.size() > 0) {
                                                    Log.e("用户数据本地化","成功获取全员会话列表:" + conversationList.size() + "");
                                                    Set<String> userIds = new HashSet<>();
                                                    final Map<String, ClassObject> conversationToClassObject = new Hashtable<>();
                                                    for (AVIMConversation tempConversation : conversationList) {
                                                        AVObject cmClassObject = conversationToClass.get(tempConversation.getConversationId());
                                                        ClassObject classObject = new ClassObject();
                                                        classObject.setCreator(cmClassObject.getString("creatorId"));
                                                        classObject.setType(0);
                                                        classObject.setClassCode(cmClassObject.getString("classCode"));
                                                        Date now = new Date();
                                                        classObject.setClassID(cmClassObject.getObjectId());
                                                        classObject.setTime(now);
                                                        classObject.setName(cmClassObject.getString("name"));
                                                        classObject.setInAcademy(cmClassObject.getString("academy"));
                                                        classObject.setInSchool(cmClassObject.getString("school"));
                                                        classObject.setTotalPeopleCount(cmClassObject.getInt("studentCount"));
                                                        classObject.setHeaderPath(cmClassObject.getString("headerImg"));
                                                        classObject.save();

                                                        conversationToClassObject.put(tempConversation.getConversationId(), classObject);
                                                        userIds.addAll(tempConversation.getMembers());
                                                    }

                                                    Log.e("用户数据本地化","所有相关用户列表:" + userIds.size() + " " + userIds.toString() );
                                                    AVQuery<AVObject> query2 = new AVQuery<>("CMUserInfo");
                                                    query2.whereContainedIn("userId", userIds);
                                                    query2.setLimit(userIds.size() * 2);
                                                    query2.findInBackground(new FindCallback<AVObject>() {
                                                        @Override
                                                        public void done(List<AVObject> list, AVException e) {
                                                            if (e == null) {
                                                                if (list.size() > 0) {
                                                                    //填充班级所有成员
                                                                    Log.e("用户数据本地化","所有相关用户信息列表:" + list.size() + " " + list.toString());
                                                                    Map<String, UserObject> idToUserObject = new HashMap<>();
                                                                    for (AVObject userInfo : list) {
                                                                        String memberId = userInfo.getString("userId");
                                                                        String memberName = userInfo.getString("realName");
                                                                        List<UserObject> temps = DataSupport.where("userId=?", memberId).find(UserObject.class);
                                                                        if (temps.size() == 0) {
                                                                            UserObject newMember = new UserObject();
                                                                            newMember.setImgId(userInfo.getString("headerImg"));
                                                                            newMember.setUserRealName(memberName);
                                                                            newMember.setUserId(memberId);
                                                                            newMember.setUserName(userInfo.getString("phone"));
                                                                            newMember.saveThrows();
                                                                            idToUserObject.put(memberId, newMember);

                                                                            UserPersonalInfo personalInfo = new UserPersonalInfo();
                                                                            personalInfo.setAcademy(userInfo.getString("academy"));
                                                                            personalInfo.setBirthday(userInfo.getDate("birthday"));
                                                                            personalInfo.setSchool(userInfo.getString("school"));
                                                                            personalInfo.setSex(userInfo.getString("sex"));
                                                                            personalInfo.setUserId(memberId);
                                                                            personalInfo.saveThrows();
                                                                        }
                                                                    }

                                                                    Log.e("用户数据本地化","准备好了一切本地源材料，进行班级成员绑定操作");
                                                                    //准备好了一切本地源材料，进行班级成员绑定操作
                                                                    for (AVIMConversation conversation : conversationList) {
                                                                        List<String> memberList = conversation.getMembers();
                                                                        ClassObject targetClass = conversationToClassObject.get(conversation.getConversationId());
                                                                        UserObject temp;
                                                                        Log.e("TAG",idToUserObject.size() + "");
                                                                        for (String userId : memberList) {
                                                                            temp = idToUserObject.get(userId);
                                                                            if (temp == null) {
                                                                                continue;
                                                                            }
                                                                            ClassMemberObject newMember = new ClassMemberObject();
                                                                            newMember.setInClass(targetClass);
                                                                            newMember.setMemberID(userId);
                                                                            newMember.setMemberName(temp.getUserRealName());
                                                                            newMember.saveThrows();
                                                                        }
                                                                    }
                                                                    Log.e("用户数据本地化","用户数据本地化完成");

                                                                    //重新获取子群
                                                                    final List<String> inSubClasses = user.getList("subClassIds");
                                                                    AVIMConversationQuery subClassQuery = CMApplication.imClient.getQuery();
                                                                    subClassQuery.whereContainsIn("objectId", inSubClasses);
                                                                    subClassQuery.findInBackground(new AVIMConversationQueryCallback() {
                                                                        @Override
                                                                        public void done(List<AVIMConversation> list, AVException e) {
                                                                            if (e == null) {
                                                                                Log.e("用户数据本地化","所有子群数据 " + inSubClasses.toString());
                                                                                final int size = list.size();
                                                                                for (int i = 0;i < size;i++) {
                                                                                    final AVIMConversation conversation = list.get(i);
                                                                                    final int finalI = i;
                                                                                    conversation.fetchInfoInBackground(new AVIMConversationCallback() {
                                                                                        @Override
                                                                                        public void done(AVException e) {
                                                                                            if (e == null) {
                                                                                                ClassObject inClass = DataSupport.where("classID=?", (String) conversation.getAttribute("classId")).
                                                                                                        find(ClassObject.class).get(0);
                                                                                                List<String> memberIds = conversation.getMembers();
                                                                                                SubClassObject newSubClass = new SubClassObject();
                                                                                                newSubClass.setTime(new Date());
                                                                                                newSubClass.setInClass(inClass);
                                                                                                newSubClass.setSubClassID(conversation.getConversationId());
                                                                                                newSubClass.setSubClassName(conversation.getName());
                                                                                                newSubClass.saveThrows();

                                                                                                Log.e("用户数据本地化", "存储子群数据 " + newSubClass.getSubClassName());

                                                                                                for (String id : memberIds) {
                                                                                                    UserObject memberObject = DataSupport.where("userId=?", id).find(UserObject.class).get(0);
                                                                                                    SubClassMemberObject newMember = new SubClassMemberObject();
                                                                                                    newMember.setInSubClass(newSubClass);
                                                                                                    newMember.setMemberID(memberObject.getUserId());
                                                                                                    newMember.setMemberName(memberObject.getUserRealName());
                                                                                                    newMember.save();
                                                                                                }

                                                                                                if (finalI == size - 1) {
                                                                                                    Log.e("TAG", "进行核心当前用户数据填充");
                                                                                                    //一切准备就绪,登陆初始化
                                                                                                    AVQuery<AVObject> curClassQuery = new AVQuery<>("UserToCurClass");
                                                                                                    curClassQuery.whereEqualTo("userId", user.getObjectId());
                                                                                                    curClassQuery.findInBackground(new FindCallback<AVObject>() {
                                                                                                        @Override
                                                                                                        public void done(List<AVObject> list, AVException e) {
                                                                                                            if (e == null) {
                                                                                                                if (list.size() > 0) {
                                                                                                                    AVObject curClassObject = list.get(0);
                                                                                                                    SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
                                                                                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                                                                    editor.putString(AppConst.USER_TO_CURCLASS, curClassObject.getObjectId());
                                                                                                                    editor.apply();
                                                                                                                    UserPersonalInfo info = DataSupport.where("userId=?", user.getObjectId()).
                                                                                                                            find(UserPersonalInfo.class).get(0);
                                                                                                                    info.setCurClassId(curClassObject.getString("curClassId"));
                                                                                                                    info.setCurClassName(curClassObject.getString("curClassName"));
                                                                                                                    info.update(info.getId());

                                                                                                                    readyToLogin(username, password);
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                                    if (progressDialog.isShowing()) {
                                                                                                        progressDialog.dismiss();
                                                                                                    }
                                                                                                }
                                                                                            } else {
                                                                                                progressDialog.dismiss();
                                                                                                Log.e("TAG", e.getMessage());
//                                                                                                                    NormalUtils.INSTANCE.showError(context, e);
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }

                                                                            } else {
                                                                                progressDialog.dismiss();
//                                                                                                    NormalUtils.INSTANCE.showError(context, e);
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            } else {
                                                                progressDialog.dismiss();
//                                                                                    NormalUtils.INSTANCE.showError(context, e);
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    progressDialog.dismiss();
                                                    Log.e("TAG","error : 无子群");
                                                }
                                            } else {
                                                progressDialog.dismiss();
//                                                                    NormalUtils.INSTANCE.showError(context, e);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void readyToLogin(final String username, final String password) {
        List<UserObject> targetUserObjects = DataSupport.where("userName=?",username).find(UserObject.class);
        final UserObject userObject = targetUserObjects.get(0);
        final UserPersonalInfo userPersonalInfo = DataSupport.where("userId=?", userObject.getUserId()).
                find(UserPersonalInfo.class).get(0);
        //初始化单聊相关数据
        ContentValues cv = new ContentValues();
        cv.put("conversationId","");
        DataSupport.updateAll(UserObject.class,cv,"userId <> ?",userObject.getUserId());

        //更新设备表的当前用户
        AVInstallation installation = AVInstallation.getCurrentInstallation();
        installation.put("userId", userObject.getUserId());
        installation.saveInBackground(new SaveCallback() {
            public void done(AVException e) {
                if (e == null) {
                    Log.e("TAG", "储存设备表成功--");
                } else {
                    Log.e("TAG", "储存设备表失败--" + e.getCode());
                }
            }
        });

        CMApplication.openIMClient(userObject.getUserId());

        AVQuery<AVObject> curClassQuery = new AVQuery<>("UserToCurClass");
        curClassQuery.whereEqualTo("userId", userObject.getUserId());
        curClassQuery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        AVObject curClassObject = list.get(0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(AppConst.CUR_CLASS_ID, userPersonalInfo.getCurClassId());
                        editor.putString(AppConst.CUR_CLASS_NAME, userPersonalInfo.getCurClassName());
                        editor.putString(AppConst.USER_HEADER_ID, userObject.getImgId());
                        editor.putString(AppConst.USER_NAME, username);
                        editor.putString(AppConst.USER_ID, userObject.getUserId());
                        editor.putString(AppConst.USER_REAL_NAME, userObject.getUserRealName());
                        editor.putString(AppConst.USER_PASSWORD, password);
                        editor.putString(AppConst.USER_TO_CURCLASS,curClassObject.getObjectId());
                        editor.putInt("start_up_count", 99);
                        editor.apply();

                        if (userPersonalInfo.getCurClassId() == null || userPersonalInfo.getCurClassId().equals("")) {
                            context.setCurFragment(BeforeMainActivity.FRAGMENT_GUIDE_PAGE);
                            FragmentManager manager = context.getSupportFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();

                            transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out,R.anim.push_left_in, R.anim.push_left_out);
                            transaction.replace(BeforeMainActivity.CONTAINER_ID, new GuidePageFragment());
                            transaction.commit();
                        } else {
                            Intent intent = new Intent(context, MainActivity.class);
                            startActivity(intent);
                            context.finish();
                        }
                    }
                }
            }
        });

    }
}
