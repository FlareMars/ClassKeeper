package com.flaremars.classmanagers.uis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassMemberObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.FragmentConst;
import com.flaremars.classmanagers.model.SubClassMemberObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.model.UserPersonalInfo;
import com.flaremars.classmanagers.utils.FlareBitmapUtils;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.Dialog;
import com.flaremars.classmanagers.views.ProgressDialog;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobObject;

public class JoinClassActivity extends FragmentActivity {

    private List<TempClassObject> items;

    private MyAdapter adapter;

    private AVUser avUser;

    private UserObject userObject;

    private ProgressDialog progressDialog;

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_class);

        SharedPreferences sharedPreferences = getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,MODE_PRIVATE);
        String userId = sharedPreferences.getString(AppConst.USER_ID, "");
        AVQuery<AVUser> query = AVUser.getQuery();
        query.getInBackground(userId, new GetCallback<AVUser>() {
            @Override
            public void done(AVUser object, AVException e) {
                avUser = object;
                userObject = DataSupport.where("userId=?",avUser.getObjectId()).find(UserObject.class).get(0);
            }
        });

        ListView listView = (ListView) findViewById(R.id.activity_join_class_content);
        items = new ArrayList<>();
        adapter = new MyAdapter(this,items);
        listView.setAdapter(adapter);

        findViewById(R.id.activity_join_class_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JoinClassActivity.this.finish();
            }
        });

        findViewById(R.id.iv_btn_join_class_create_class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JoinClassActivity.this,ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_CREATE_CLASS);
                intent.putExtras(bundle);
                startActivity(intent);
                JoinClassActivity.this.finish();
            }
        });

        final EditText input = (EditText) findViewById(R.id.activity_join_class_input);

        findViewById(R.id.activity_join_class_clean).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input.setText("");
            }
        });


        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        closeSoftInput();
                        String key = input.getText().toString();
                        if (key.equals("")) {
                            Toast.makeText(JoinClassActivity.this,"输入不能为空",Toast.LENGTH_LONG).show();
                        } else {
                            new GetClassesTask().execute(key);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        findViewById(R.id.activity_join_class_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = input.getText().toString();
                if (key.equals("")) {
                    Toast.makeText(JoinClassActivity.this,"输入不能为空",Toast.LENGTH_LONG).show();
                } else {
                    new GetClassesTask().execute(key);
                }
            }
        });
    }

    private class GetClassesTask extends AsyncTask<String, TempClassObject, Void> {

        //这是一个异步过程，当查询请求发出后，方法马上返回，result并还没有得到填充
        @Override
        protected Void doInBackground(String... params) {
            String key = params[0];
            final TempClassObject result = new TempClassObject("","","",0,"","");
            AVQuery<AVObject> classQuery = new AVQuery<>("CMClassObject");
            classQuery.whereEqualTo("classCode", key);
            classQuery.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    if (e == null) {
                        if (list.size() > 0) {
                            AVObject classObject = list.get(0);
                            result.setName(classObject.getString("name"));
                            result.setAcademy(classObject.getString("academy"));
                            result.setClassID(classObject.getObjectId());
                            result.setSchool(classObject.getString("school"));
                            result.setPeopleNum(classObject.getInt("studentCount"));
                            result.setImg(classObject.getString("headerImg"));
                            publishProgress(result);
                        } else {
                            NormalUtils.INSTANCE.showToast(JoinClassActivity.this, "对不起，没有找到符合条件的班级");
                        }
                    } else {
                        NormalUtils.INSTANCE.showError(JoinClassActivity.this,e);
                    }
                }
            });

            return null;
        }

        @Override
        protected void onProgressUpdate(TempClassObject...values) {
            TempClassObject tempClassObject = values[0];
            if (tempClassObject.classID.equals("")) {
                Toast.makeText(JoinClassActivity.this,"对不起，没有找到符合条件的班级",Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(JoinClassActivity.this,"找到啦~",Toast.LENGTH_LONG).show();
                items.clear();
                items.add(tempClassObject);
                adapter.notifyDataSetChanged();
            }
        }

    }
    private class TempClassObject extends BmobObject{
        private String name;

        private int peopleNum;

        private String school;

        private String academy;

        private String img;

        private String classID;

        private TempClassObject(String school,String academy, String img, int peopleNum, String name,String classID) {
            this.school = school;
            this.academy = academy;
            this.img = img;
            this.peopleNum = peopleNum;
            this.name = name;
            this.classID = classID;
        }

        public String getSchool() {
            return school;
        }

        public void setSchool(String school) {
            this.school = school;
        }

        public String getAcademy() {
            return academy;
        }

        public void setAcademy(String academy) {
            this.academy = academy;
        }

        public String getClassID() {
            return classID;
        }

        public void setClassID(String classID) {
            this.classID = classID;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPeopleNum() {
            return peopleNum;
        }

        public void setPeopleNum(int peopleNum) {
            this.peopleNum = peopleNum;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }
    }

    private class MyAdapter extends BaseAdapter {

        private List<TempClassObject> items;

        private Context context;

        public MyAdapter(Context context,List<TempClassObject> items) {
            this.items = items;
            this.context = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public TempClassObject getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            public ImageView classImg;

            public TextView className;

            public TextView classPeopleNum;

            public TextView classSchoolAcademy;

            public TextView joinClass;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TempClassObject item = getItem(position);
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_join_class,null);
                viewHolder = new ViewHolder();
                viewHolder.classImg = (ImageView) convertView.findViewById(R.id.item_join_class_img);
                viewHolder.className = (TextView) convertView.findViewById(R.id.item_join_class_name);
                viewHolder.classPeopleNum = (TextView) convertView.findViewById(R.id.item_join_class_people_num);
                viewHolder.classSchoolAcademy = (TextView) convertView.findViewById(R.id.item_join_class_school_academy);
                viewHolder.joinClass = (TextView) convertView.findViewById(R.id.item_join_class_join);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.classPeopleNum.setText(item.getPeopleNum() + "人");
            viewHolder.className.setText(item.getName() + "|");
            viewHolder.classSchoolAcademy.setText(item.getSchool() + " |\n " + item.getAcademy());
//            BitmapUtils.INSTANCE.loadBitmap(viewHolder.classImg, item.getImg());
            if (item.getImg().equals("")) {
                viewHolder.classImg.setImageResource(R.drawable.default_ic_mine_class);
            } else {
                FlareBitmapUtils.INSTANCE.loadBitmap(viewHolder.classImg,item.getImg());
            }

            viewHolder.joinClass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*创建属于自己的Member,查询目标班级和子群*/
                    final Dialog dialog = new Dialog(JoinClassActivity.this,"提示","确认加入 \n【" + item.getName() + "】吗");
                    dialog.addCancelButton("取消");
                    dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            joinClassTask(item.getClassID());
                        }
                    });
                    dialog.show();
                }
            });
            return convertView;
        }
    }

    private void joinClassTask(final String classID) {
        SharedPreferences preferences = getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, MODE_PRIVATE);
        final String userId = preferences.getString(AppConst.USER_ID, "");
        if (userId == null || userId.equals("")) {
            Log.e("TAG","严重出错 ： 用户ID为空");
            return;
        }
//        List<ClassObject> temps = DataSupport.findAll(ClassObject.class);
//        List<String> classIds = new ArrayList<>();
//        for (ClassObject temp : temps) {
//            classIds.add(temp.getClassID());
//        }
//        if (classIds.size() > 0) {
//          if (classIds.contains(classID)) {
//          }
//        }
        List<ClassObject> checkClass = DataSupport.where("classID=?",classID).find(ClassObject.class);
        if (checkClass.size() > 0) {
            ClassObject target = checkClass.get(0);
            List<String> memberIds = target.getMemberIds();
            if (memberIds.contains(userId)) {
              NormalUtils.INSTANCE.showToast(JoinClassActivity.this, "你已经在本班了，请勿重复加入~");
              return;
            }
        }

        //其他情况一律通过
        progressDialog = new ProgressDialog(JoinClassActivity.this,"正在处理，请稍候~");
        progressDialog.show();

        //首先查询CMClassObject
        final AVQuery<AVObject> query = new AVQuery<>("CMClassObject");
        query.getInBackground(classID, new GetCallback<AVObject>() {
            @Override
                public void done(final AVObject targetClass, AVException e) {
                if (e == null) {
                    //进入全员会话(子群)
                    {
                        AVIMConversationQuery query1 = CMApplication.imClient.getQuery();
                        query1.whereEqualTo("objectId", targetClass.getString("allMembersConversation"));
                        query1.findInBackground(new AVIMConversationQueryCallback() {
                            @Override
                            public void done(List<AVIMConversation> list, AVException e) {
                                if (e == null) {
                                    if (list.size() > 0) {
                                        final AVIMConversation targetConversation = list.get(0);
                                        targetConversation.join(new AVIMConversationCallback() {
                                            @Override
                                            public void done(AVException e) {
                                                if (e == null) {
                                                    //网络工作完成，进行本地化
                                                    final ClassObject classObject;
                                                    final SubClassObject subClassObject;
                                                    List<ClassObject> checkClass = DataSupport.where("classID=?",classID).find(ClassObject.class);
                                                    if (checkClass.size() > 0) {
                                                        classObject = checkClass.get(0);
                                                        classObject.setTotalPeopleCount(targetClass.getInt("studentCount") + 1);
                                                        classObject.update(classObject.getId());

                                                        subClassObject = DataSupport.where("subClassID=?",targetConversation.getConversationId()).find(SubClassObject.class).get(0);
                                                    } else {
                                                        classObject = new ClassObject();
                                                        classObject.setCreator(targetClass.getString("creatorId"));
                                                        classObject.setType(0);
                                                        classObject.setClassCode(targetClass.getString("classCode"));
                                                        Date now = new Date();
                                                        classObject.setClassID(targetClass.getObjectId());
                                                        classObject.setTime(now);
                                                        classObject.setName(targetClass.getString("name"));
                                                        classObject.setInAcademy(targetClass.getString("academy"));
                                                        classObject.setInSchool(targetClass.getString("school"));
                                                        classObject.setTotalPeopleCount(targetClass.getInt("studentCount") + 1);
                                                        classObject.setHeaderPath(targetClass.getString("headerImg"));
                                                        classObject.save();

                                                        subClassObject = new SubClassObject();
                                                        subClassObject.setSubClassID(targetConversation.getConversationId());
                                                        subClassObject.setSubClassName(targetConversation.getName());
                                                        subClassObject.setTime(new Date());
                                                        subClassObject.setInClass(classObject);
                                                        subClassObject.save();

                                                        List<String> members = targetConversation.getMembers();

                                                        AVQuery<AVObject> query2 = new AVQuery<>("CMUserInfo");
                                                        query2.whereContainedIn("userId", members);

                                                        query2.setLimit(members.size());
                                                        query2.findInBackground(new FindCallback<AVObject>() {
                                                            @Override
                                                            public void done(List<AVObject> list, AVException e) {
                                                                if (e == null) {
                                                                    if (list.size() > 0) {

                                                                        Log.e("TAG", list.size() + "");
                                                                        //填充班级所有成员
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
                                                                                newMember.save();
                                                                            }

                                                                            ClassMemberObject tempClassMember = new ClassMemberObject();
                                                                            tempClassMember.setInClass(classObject);
                                                                            tempClassMember.setMemberID(memberId);
                                                                            tempClassMember.setMemberName(memberName);
                                                                            tempClassMember.save();

                                                                            SubClassMemberObject tempSubClassNewMember = new SubClassMemberObject();
                                                                            tempSubClassNewMember.setInSubClass(subClassObject);
                                                                            tempSubClassNewMember.setMemberID(memberId);
                                                                            tempSubClassNewMember.setMemberName(memberName);
                                                                            tempSubClassNewMember.save();
                                                                        }
                                                                    }
                                                                } else {
                                                                    Log.e("TAG2", e.getMessage());
                                                                    NormalUtils.INSTANCE.showError(JoinClassActivity.this, e);
                                                                }
                                                            }
                                                        });
                                                    }

                                                    ClassMemberObject newMember = new ClassMemberObject();
                                                    newMember.setInClass(classObject);
                                                    newMember.setMemberID(userObject.getUserId());
                                                    newMember.setMemberName(userObject.getUserRealName());
                                                    newMember.save();

                                                    SubClassMemberObject subClassNewMember = new SubClassMemberObject();
                                                    subClassNewMember.setInSubClass(subClassObject);
                                                    subClassNewMember.setMemberID(userObject.getUserId());
                                                    subClassNewMember.setMemberName(userObject.getUserRealName());
                                                    subClassNewMember.save();

                                                    final UserPersonalInfo userPersonalInfo = DataSupport.where("userId=?", userObject.getUserId()).
                                                            find(UserPersonalInfo.class).get(0);

                                                    //如果无当前班级，则直接将新班作为当前班级
                                                    final String classId = targetClass.getObjectId();
                                                    final String className = targetClass.getString("name");
                                                    if (MainActivity.BASE_GLOBAL_DATA.getCurClassID() == null ||
                                                            MainActivity.BASE_GLOBAL_DATA.getCurClassID().equals("")) {

                                                        MainActivity.BASE_GLOBAL_DATA.setCurClassID(classId);
                                                        MainActivity.BASE_GLOBAL_DATA.setCurClassName(className);

                                                        userPersonalInfo.setCurClassName(className);
                                                        userPersonalInfo.setCurClassId(classId);
                                                        userPersonalInfo.update(userPersonalInfo.getId());

                                                        SharedPreferences preferences = getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = preferences.edit();
                                                        editor.putString(AppConst.CUR_CLASS_ID, classId);
                                                        editor.putString(AppConst.CUR_CLASS_NAME, className);
                                                        editor.apply();

                                                        //更新用户信息中的当前班级
                                                        AVQuery<AVObject> query = new AVQuery<>("UserToCurClass");

                                                        SharedPreferences sharedPreferences = JoinClassActivity.this.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
                                                        query.getInBackground(sharedPreferences.getString(AppConst.USER_TO_CURCLASS, ""), new GetCallback<AVObject>() {
                                                            @Override
                                                            public void done(AVObject avObject, AVException e) {
                                                                avObject.put("curClassId", classId);
                                                                avObject.put("curClassName", className);
                                                                avObject.saveInBackground();
                                                            }
                                                        });
                                                    }

                                                    //更新班级数据
                                                    AVRelation<AVUser> members = targetClass.getRelation("members");
                                                    members.add(avUser);
                                                    targetClass.increment("studentCount");
                                                    targetClass.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(AVException e) {
                                                            if (e == null) {
                                                                //更新用户数据
                                                                AVRelation<AVObject> inClasses = avUser.getRelation("inClasses");
                                                                inClasses.add(targetClass);
                                                                avUser.add("subClassIds", targetClass.getString("allMembersConversation"));
                                                                avUser.saveInBackground();
                                                            } else {
                                                                NormalUtils.INSTANCE.showError(JoinClassActivity.this,e);
                                                            }
                                                        }
                                                    });

                                                    //加入工作完成
                                                    progressDialog.dismiss();
                                                    Toast.makeText(JoinClassActivity.this, "加入成功", Toast.LENGTH_LONG).show();

                                                    //判断当前用户是否填写了“学校”与“学院”，如果无，则询问是否自动根据加入的班级进行填充
                                                    if (userPersonalInfo.getAcademy() == null || userPersonalInfo.getAcademy().equals("")) {

                                                        String format = "您所在的学校/学院信息为:\n%s\n%s";
                                                        final Dialog dialog = new Dialog(JoinClassActivity.this,"修改信息",
                                                                String.format(format,classObject.getInSchool(),classObject.getInAcademy()));
                                                        dialog.addCancelButton("取消");
                                                        dialog.setButtonAcceptText("确认修改");
                                                        dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                AVQuery<AVObject> userInfoQuery = new AVQuery<>("CMUserInfo");
                                                                userInfoQuery.whereEqualTo("userId", avUser.getObjectId());
                                                                userInfoQuery.findInBackground(new FindCallback<AVObject>() {
                                                                    @Override
                                                                    public void done(List<AVObject> list, AVException e) {
                                                                        if (e == null) {
                                                                            if (list.size() > 0) {
                                                                                AVObject infoUpdate = list.get(0);
                                                                                ClassObject classObject = DataSupport.where("classID=?", targetClass.getObjectId()).find(ClassObject.class).get(0);
                                                                                infoUpdate.put("school", classObject.getInSchool());
                                                                                infoUpdate.put("academy", classObject.getInAcademy());
                                                                                infoUpdate.saveInBackground();

                                                                                userPersonalInfo.setSchool(classObject.getInSchool());
                                                                                userPersonalInfo.setAcademy(classObject.getInAcademy());
                                                                                userPersonalInfo.update(userPersonalInfo.getId());
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });
                                                        dialog.show();
                                                    }

                                                    Intent intent = new Intent(JoinClassActivity.this, MainActivity.class);
                                                    startActivity(intent);

                                                    Intent finishActivityBroadCast = new Intent("FRAGMENT_TO_FINISH");
                                                    sendBroadcast(finishActivityBroadCast);
                                                    JoinClassActivity.this.finish();

                                                } else {
                                                    Log.e("TAG-Join", e.getMessage());
                                                    NormalUtils.INSTANCE.showError(JoinClassActivity.this, e);
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("TAG", "寻找全员子群出错");
                                    }
                                } else {

                                    Log.e("TAG", e.getMessage());
                                    NormalUtils.INSTANCE.showError(JoinClassActivity.this, e);
                                }
                            }
                        });
                    }
                } else {
                    NormalUtils.INSTANCE.showError(JoinClassActivity.this, e);
                }
            }
        });

    }

    private void closeSoftInput() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
