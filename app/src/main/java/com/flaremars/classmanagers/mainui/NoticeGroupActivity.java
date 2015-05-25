package com.flaremars.classmanagers.mainui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.DataWrapper;
import com.flaremars.classmanagers.model.NoticeFeedbackObject;
import com.flaremars.classmanagers.model.NoticeGroupMemberObject;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.NoticeTaskObject;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.uis.ContainerActivity;
import com.flaremars.classmanagers.uis.NoticeGroupLayout;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.FragmentConst;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.utils.MessagePostUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeGroupActivity extends Activity {

//    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd hh:mm", Locale.CHINA);

    private static final int ACTION_SELECT_CONTACTS = 1;

    private static final int ACTION_SEND_EXCEL = 4;

    private EditText inputEditText;

    private LinearLayout inputLayout;

    private NoticeGroupLayout childView;

    private int targetNoticeGroup;

    private NoticeGroupObject noticeGroupObject;

    private ClassObject classObject;

    private UserObject mineObject;

    private TextView membersTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_group);

//        Bmob.initialize(this, AppConst.APP_ID);
//        BmobPush.startWork(this, AppConst.APP_ID);

        targetNoticeGroup = getIntent().getIntExtra("target_notice_group", 1);
        boolean isInitInputMode = getIntent().getBooleanExtra("is_show_input_layout", false);

        noticeGroupObject = DataSupport.find(NoticeGroupObject.class, targetNoticeGroup);
        //由于进入通知组界面的不一定是只能进入当前班级通知组，所以这里要根据noticegroupobject获取真正的班级
        Cursor classIdCursor = DataSupport.findBySQL("select classobject_id from noticegroupobject where id=?",
                String.valueOf(noticeGroupObject.getId()));
        if (classIdCursor != null && classIdCursor.moveToFirst()) {
            classObject = DataSupport.find(ClassObject.class, classIdCursor.getLong(0));
        } else {
            Log.e("TAG", "出错，查找班级失败");
            return;
        }

        mineObject = DataSupport.where("userId=?",MainActivity.BASE_GLOBAL_DATA.getUserID()).find(UserObject.class).get(0);
        FrameLayout layout = (FrameLayout) findViewById(R.id.fl_notice_group_ui_content);
        childView = new NoticeGroupLayout(this,null);
        childView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        childView.setNoticeGroupObject(noticeGroupObject);

        List<NoticeGroupMemberObject> members = noticeGroupObject.getMemberList();
        int count = 0;
        StringBuilder sb = new StringBuilder();
        for (NoticeGroupMemberObject user : members) {
            sb.append(user.getMemberName());
            sb.append(" ");
            count++;
            if (count == 6) {
                sb.append("\n");
                count = 0;
            }
        }
        membersTextView = (TextView) childView.findViewById(R.id.tv_notice_group_ui_members);
        membersTextView.setText(sb.toString());

        layout.addView(childView);

        findViewById(R.id.iv_btn_notice_group_ui_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开选择联系人界面
                Intent intent = new Intent(NoticeGroupActivity.this, SelectContactsActivity.class);
                startActivityForResult(intent, ACTION_SELECT_CONTACTS);
            }
        });

        findViewById(R.id.btn_notice_group_ui_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoticeGroupActivity.this.finish();
            }
        });

        ImageView emotionIvBtn = (ImageView) findViewById(R.id.iv_btn_chatting_emotion);
        inputEditText = (EditText) findViewById(R.id.et_chatting_input);
        TextView sendTvBtn = (TextView) findViewById(R.id.tv_btn_chatting_send);
        inputLayout = (LinearLayout) findViewById(R.id.ll_input_layout);

        if (isInitInputMode) {
            setInputLayoutVisible(true);
            childView.setSignForInputLayout();
        }

        emotionIvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        sendTvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputEditText.getText().toString();
                if (!content.equals("")) {
                    String title;
                    if (content.length() > 10) {
                        title = content.substring(0,10);
                        title += "...";
                    } else {
                        title = content;
                    }
                    NoticeTaskObject noticeTaskObject = new NoticeTaskObject();
                    noticeTaskObject.setNewFeedback(false);
                    noticeTaskObject.setName(title);
                    Date now = new Date();
                    noticeTaskObject.setTime(now);
                    noticeTaskObject.setContent(content);
                    noticeTaskObject.setInNoticeGroup(noticeGroupObject);
                    noticeTaskObject.setNoticeId(new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA).format(now));
                    noticeTaskObject.save();

                    //创建并发出通知相关项目，反馈和任务
                    final List<String> targetIds = new ArrayList<>();
                    List<NoticeGroupMemberObject> targets = noticeGroupObject.getMemberList();
                    for (NoticeGroupMemberObject temp : targets) {
                        targetIds.add(temp.getMemberID());

                        NoticeFeedbackObject feedbackObject = new NoticeFeedbackObject();
                        UserObject userObject = DataSupport.where("userID=?",temp.getMemberID()).find(UserObject.class).get(0);
                        feedbackObject.setWhose(temp.getMemberID());
                        feedbackObject.setInNoticeTaskObject(noticeTaskObject);
                        feedbackObject.setTargetPhone(userObject.getUserName());
                        feedbackObject.setDone(false);
                        feedbackObject.setWhoseName(temp.getMemberName());
                        feedbackObject.save();

                    }

                    final JSONObject msgContent = new JSONObject();
                    try {
                        msgContent.put(MessageConst.MSG_TYPE,MessageConst.NEW_NOTICE_COMING);
                        msgContent.put(MessageConst.CONTENT_FROM,MainActivity.BASE_GLOBAL_DATA.getUserRealName());
                        msgContent.put(MessageConst.CONTENT_FROM_ID,MainActivity.BASE_GLOBAL_DATA.getUserID());
                        msgContent.put(MessageConst.CONTENT_TARGET_ID,noticeTaskObject.getNoticeId());
                        msgContent.put(MessageConst.CONTENT_NOTICE_CONTENT,noticeTaskObject.getContent());
                        msgContent.put(MessageConst.CONTENT_NOTICE_NAME,noticeTaskObject.getName());
                        msgContent.put(MessageConst.CONTENT_IN_CLASS,classObject.getClassID());
                        msgContent.put(MessageConst.CONTENT_TIME,noticeTaskObject.getTime().getTime());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //创建或更新通知组入口
                    updateRecentMessage(classObject, title);

                    DataWrapper wrapper = new DataWrapper(
                            MainActivity.BASE_GLOBAL_DATA.getUserID(),
                            classObject.getClassID(),
                            "",
                            targetIds,
                            msgContent
                    );

                    int messageId = MessagePostUtil.INSTANCE.wannaPostMessage(wrapper);
                    Log.e("TAG", "messageId = " + messageId);

                    childView.addItem(content, 0, noticeTaskObject.getTime(),noticeTaskObject.getId());
                    inputEditText.setText("");
                    setInputLayoutVisible(false);
                }
            }
        });
    }



    /*创建通知组的最近消息*/
    private void updateRecentMessage(ClassObject classObject,String content) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_SELECT_CONTACTS) {
            if (resultCode == RESULT_OK) {
                String contactsStr = data.getStringExtra("contacts_data");
                String [] contacts = contactsStr.split("_");
                List<NoticeGroupMemberObject> curMembers = noticeGroupObject.getMemberList();
                for (String target : contacts) {
                    UserObject temp = DataSupport.where("userID=?",target).find(UserObject.class).get(0);
                    NoticeGroupMemberObject tempMember = new NoticeGroupMemberObject();
                    tempMember.setMemberID(temp.getUserId());
                    if (!curMembers.contains(tempMember)) {
                        tempMember.setMemberName(temp.getUserRealName());
                        tempMember.setInNoticeGroup(noticeGroupObject);
                        tempMember.save();
                        curMembers.add(tempMember);
                    }

                    //如果是为自定义名字，就更新通知组名字
                    if (!noticeGroupObject.isCustomName()) {
                        final StringBuilder nameStringBuilder = new StringBuilder();
                        for (NoticeGroupMemberObject tempObject : curMembers) {
                            nameStringBuilder.append(tempObject.getMemberName());
                            nameStringBuilder.append("、");
                        }
                        nameStringBuilder.deleteCharAt(nameStringBuilder.length() - 1);
                        noticeGroupObject.setName(nameStringBuilder.toString());
                        noticeGroupObject.update(noticeGroupObject.getId());
                    }
                }
                //更新界面
                int count = 0;
                StringBuilder sb = new StringBuilder();
                for (NoticeGroupMemberObject user : curMembers) {
                    sb.append(user.getMemberName());
                    sb.append(" ");
                    count++;
                    if (count == 6) {
                        sb.append("\n");
                        count = 0;
                    }
                }
                membersTextView.setText(sb.toString());
            } else {
                Toast.makeText(NoticeGroupActivity.this,"没有新增任何联系人",Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == ACTION_SEND_EXCEL) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("model_name");
                long excelTaskObjectId = data.getIntExtra("excelTaskObject_id", 1);
                childView.addItem(name, R.mipmap.ic_excel_mid,new Date(),excelTaskObjectId);

                updateRecentMessage(classObject,name);
            } else {
                Toast.makeText(this,"无效信息录制模板",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendExcelModel() {
        Intent intent = new Intent(NoticeGroupActivity.this, ContainerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_EXCEL_MODELS);
        bundle.putInt(AppConst.TARGET_NOTICE_GROUP, targetNoticeGroup);
        bundle.putBoolean(AppConst.IS_POINT_TO_PERSON,false);
        intent.putExtras(bundle);
        startActivityForResult(intent, ACTION_SEND_EXCEL);
    }

    public void setInputLayoutVisible(boolean show) {
        if (show) {
            inputLayout.setVisibility(View.VISIBLE);
            inputEditText.requestFocus();
        } else {
            inputLayout.setVisibility(View.GONE);
        }
    }
}
