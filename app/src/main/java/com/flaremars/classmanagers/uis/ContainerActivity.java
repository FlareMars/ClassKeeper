package com.flaremars.classmanagers.uis;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.LocalPicturesActivity;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.mainui.SelectContactsActivity;
import com.flaremars.classmanagers.mainui.SelectFilesActivity;
import com.flaremars.classmanagers.model.AlbumsObject;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassMemberObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.DataWrapper;
import com.flaremars.classmanagers.model.ExcelFeedbackObject;
import com.flaremars.classmanagers.model.ExcelTaskObject;
import com.flaremars.classmanagers.model.FileObject;
import com.flaremars.classmanagers.model.FileType;
import com.flaremars.classmanagers.model.FragmentConst;
import com.flaremars.classmanagers.model.ManagerObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.NoticeGroupMemberObject;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.RecentNoticeGroupMsg;
import com.flaremars.classmanagers.model.SubClassMemberObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.utils.ExcelExportor;
import com.flaremars.classmanagers.utils.FileUtils;
import com.flaremars.classmanagers.utils.MessagePostUtil;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.utils.UploadUtils;
import com.flaremars.classmanagers.views.CircleLoadingView;
import com.flaremars.classmanagers.views.ProgressBarCircularIndeterminate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerActivity extends FragmentActivity {

    private TextView titleView;

    private List<Integer> actionViews = new ArrayList<>(); //记录actionView的id

    private OnBackListener backListener;

    private String targetClass;

    private CircleLoadingView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        ImageView backView = (ImageView) findViewById(R.id.fragment_container_backView);
        titleView = (TextView) findViewById(R.id.fragment_container_titleTextView);
        loadingView = (CircleLoadingView) findViewById(R.id.fragment_container_loading_view);

        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backListener.onBack();
            }
        });

        initActionViews();
        Bundle bundle = getIntent().getExtras();
        //根据传入的参数设置第一个fragment
        FragmentConst targetFragment = (FragmentConst)bundle.getSerializable(AppConst.TARGET_FRAGMENT);

        targetClass = "";
        String targetNoticeGroup;
        int intParam;
        boolean isAddToBackStack;

        switch (targetFragment) {
            case FRAGMENT_CLASS_CENTER:
                targetClass = bundle.getString(AppConst.CLASS_ID);
                replaceFragmentNoAnimation(ClassInfoFragment.newInstance(targetClass), false);
                break;
            case FRAGMENT_ANNOUNCEMENTS_LIST:
                targetClass = bundle.getString(AppConst.CLASS_ID);
                intParam = bundle.getInt(AppConst.TARGET_ANNOUNCEMENT);
                isAddToBackStack = bundle.getBoolean(AppConst.NEED_ADD_TO_BACK_STACK);
                replaceFragmentNoAnimation(AnnouncementListFragment.newInstance(targetClass, intParam), isAddToBackStack);
                break;
            case FRAGMENT_CONTACTS:
                targetClass = bundle.getString(AppConst.CLASS_ID);
                isAddToBackStack = bundle.getBoolean(AppConst.NEED_ADD_TO_BACK_STACK);
                replaceFragmentNoAnimation(ContactsFragment.newInstance(targetClass), isAddToBackStack);
                break;
            case FRAGMENT_FILES:
                checkUploadState();
                targetClass = bundle.getString(AppConst.CLASS_ID);
                isAddToBackStack = bundle.getBoolean(AppConst.NEED_ADD_TO_BACK_STACK);
                replaceFragmentNoAnimation(FilesFragment.newInstance(targetClass), isAddToBackStack);
                break;
            case FRAGMENT_ALBUMS:
                targetClass = bundle.getString(AppConst.CLASS_ID);
                replaceFragmentNoAnimation(AlbumsFragment.newInstance(targetClass), false);
                break;
            case FRAGMENT_EXCEL_MODELS:
                int targetId = bundle.getInt(AppConst.TARGET_NOTICE_GROUP);
                targetNoticeGroup = String.valueOf(targetId);
                boolean isPointToPerson = bundle.getBoolean(AppConst.IS_POINT_TO_PERSON,false);
                replaceFragmentNoAnimation(ModelSelectionFragment.newInstance(targetNoticeGroup, isPointToPerson), false);
                break;
            case FRAGMENT_CREATE_ANNOUNCEMENT:
                targetClass = bundle.getString(AppConst.CLASS_ID);
                replaceFragmentNoAnimation(CreateAnnouncementFragment.newInstance(targetClass), false);
                break;
            case FRAGMENT_PERSONAL_CENTER:
                replaceFragmentNoAnimation(new PersonalCenterFragment(), false);
                break;
            case FRAGMENT_OTHER_CLASS_MESSAGE:
                replaceFragmentNoAnimation(new OtherMessagesFragment(), false);
                break;
            case FRAGMENT_DOWNLOADED_FILES:
                replaceFragmentNoAnimation(new DownloadedFilesFragment(), false);
                break;
            case FRAGMENT_SETTING:
                replaceFragmentNoAnimation(new SettingFragment(), false);
                break;
            case FRAGMENT_NOTICE_GROUPS:
                replaceFragmentNoAnimation(new NoticeGroupsFragment(), false);
                break;
            case FRAGMENT_FILL_EXCEL:
                int target = bundle.getInt(AppConst.TARGET_EXCEL);
                replaceFragmentNoAnimation(FillInfoCollectionFragment.newInstance(target), false);
                break;
            case FRAGMENT_CREATE_CLASS:
                replaceFragmentNoAnimation(new CreateClassFragment(), false);
                break;
            case FRAGMENT_NOTICE_DETAIL:
                String nameOfSender = bundle.getString("nameOfSender");
                String time = bundle.getString("time");
                String content = bundle.getString("content");
                replaceFragmentNoAnimation(NoticeDetailFragment.newInstance(nameOfSender, time, content), false);
                break;
            case FRAGMENT_PHOTOS:
                checkUploadState();
                String targetAlbumId = bundle.getString("target_album_id");
                int targetClassId = bundle.getInt("targetClass");
                replaceFragmentNoAnimation(PhotosFragment.newInstance(targetAlbumId,targetClassId), false);
                break;
            case FRAGMENT_FEEDBACK_DETAIL:
                int targetObjectId = bundle.getInt("target_object_id",0);
                String targetName = bundle.getString("target_name");
                int type = bundle.getInt("target_type",0);
                replaceFragmentNoAnimation(FeedbackDetailFragment.newInstance(targetName,targetObjectId,type),false);
            default:
                break;
        }

    }

    private void initActionViews() {
        actionViews = new ArrayList<>();
        actionViews.add(R.id.main_container_create_class);
        actionViews.add(R.id.main_container_create_announcement);
        actionViews.add(R.id.main_container_create_album);
        actionViews.add(R.id.main_container_create_model);
        actionViews.add(R.id.main_container_upload_file);
        actionViews.add(R.id.main_container_popup_window);
        actionViews.add(R.id.main_container_contacts_confirm);
        actionViews.add(R.id.main_container_export_excel);

        //下面是对actionViews的点击事件进行响应
        findViewById(R.id.main_container_create_class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new CreateClassFragment(), true);
            }
        });

        findViewById(R.id.main_container_create_announcement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targetClass = getIntent().getExtras().getString(AppConst.CLASS_ID);
                replaceFragment(CreateAnnouncementFragment.newInstance(targetClass), true);
            }
        });

        findViewById(R.id.main_container_create_model).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //可能是个人也有可能是通知组
                Bundle bundle = getIntent().getExtras();
                int targetNoticeGroup = bundle.getInt(AppConst.TARGET_NOTICE_GROUP);
                String initData = "";
                boolean isPointToPerson = bundle.getBoolean(AppConst.IS_POINT_TO_PERSON, false);
                replaceFragment(CreateInfoCollectionFragment.newInstance(String.valueOf(targetNoticeGroup)
                        ,initData,isPointToPerson),true);
            }
        });

        findViewById(R.id.main_container_upload_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContainerActivity.this, SelectFilesActivity.class);
                intent.putExtra("file_select_mode",SelectFilesActivity.FILE_SELECT_MODE.ALL_KIND);
                startActivityForResult(intent, SelectFilesActivity.ACTION_SELECT_FILES);
            }
        });


        findViewById(R.id.main_container_popup_window).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClassObject targetClassObject = DataSupport.where("classID=?",targetClass).find(ClassObject.class).get(0);
                List<ManagerObject> managers = DataSupport.where("classobject_id=?", String.valueOf(targetClassObject.getId())).find(ManagerObject.class);
                String creator = targetClassObject.getCreator();
                boolean isFull = false;
                if (creator.equals(MainActivity.BASE_GLOBAL_DATA.getUserID())) {
                    isFull = true;
                } else {
                    if (managers.size() > 0) {
                        isFull = true;
                    }
                }
                AddGroupPopupWindow.showPopupWindow(v,isFull);
                AddGroupPopupWindow.setOnActionClickListener(new AddGroupPopupWindow.OnActionClickListener() {
                    @Override
                    public void onActionClickListener(int action) {
                        int requestCode;
                        if (action == AddGroupPopupWindow.ACTION_ADD_NOTICE_GROUP) {
                            requestCode = AddGroupPopupWindow.ACTION_ADD_NOTICE_GROUP;
                        } else {
                            requestCode = AddGroupPopupWindow.ACTION_ADD_GROUP;
                        }
                        Intent intent = new Intent(ContainerActivity.this, SelectContactsActivity.class);
                        startActivityForResult(intent, requestCode);
                    }
                });
            }
        });

        findViewById(R.id.main_container_create_album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClassObject targetClassObject = DataSupport.where("classID=?",String.valueOf(targetClass)).
                        find(ClassObject.class).get(0);
                CreateAlbumDialog createAlbumDialog = CreateAlbumDialog.getFunctionDialog(targetClassObject.getId());
                createAlbumDialog.show(getSupportFragmentManager(), "create_album");
            }
        });

        findViewById(R.id.main_container_upload_photos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContainerActivity.this, LocalPicturesActivity.class);
                startActivityForResult(intent,PhotosFragment.ACTION_UPLOAD_PHOTO);
            }
        });

        findViewById(R.id.main_container_export_excel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int targetObjectId = getIntent().getExtras().getInt("target_object_id", 0);
                ExcelTaskObject excelTaskObject = DataSupport.find(ExcelTaskObject.class, targetObjectId);
                List<ExcelFeedbackObject> feedbackObjects = excelTaskObject.getFeedbackObjectList();

                JSONArray answerArray = new JSONArray();
                for (ExcelFeedbackObject object : feedbackObjects) {
                    JSONObject temp = new JSONObject();
                    try {
                        temp.put("whose",object.getWhoseName());
                        temp.put("answers",object.getAnswer());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    answerArray.put(temp);
                }

                String result = ExcelExportor.getInstance().toExcel(answerArray,excelTaskObject);

                String targetName = excelTaskObject.getName() + ".xls";
                List<FileObject> temp = DataSupport.where("name=?",targetName).find(FileObject.class);
                if (temp.size() == 0) {
                    FileObject newFileObject = new FileObject();
                    newFileObject.setTime(new Date());
                    newFileObject.setPath(result);
                    newFileObject.setUpdateTime(new Date());
                    newFileObject.setName(excelTaskObject.getName() + ".xls");
                    newFileObject.setSource("信息录制制表");
                    newFileObject.setType(FileType.DOCUMENT.ordinal());
                    newFileObject.setSize(new File(result).length());
                    newFileObject.save();
                } else {
                    FileObject fileObject = temp.get(0);
                    fileObject.setUpdateTime(new Date());
                    fileObject.setTime(new Date());
                    fileObject.setSize(new File(fileObject.getPath()).length());
                    fileObject.update(fileObject.getId());
                }

                NormalUtils.INSTANCE.showToast(ContainerActivity.this,"制表完成，可到“已下载”中查看");
                FileUtils.getInstance().openFile(new File(result),ContainerActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //这句很重要，只有它将Result分发给activity的Fragment
        super.onActivityResult(requestCode, resultCode, data);
        if (!NormalUtils.INSTANCE.isNetworkRegularWork(ContainerActivity.this)) {
            NormalUtils.INSTANCE.showToast(ContainerActivity.this,"网络错误，请检查网络配置");
            return;
        }

        List<ClassObject> temp = DataSupport.where("classID=?",MainActivity.BASE_GLOBAL_DATA.getCurClassID()).find(ClassObject.class);
        ClassObject curClass;
        if (temp.size() == 0) {
            return;
        } else {
            curClass = temp.get(0);
        }
        switch (requestCode) {
            case SelectFilesActivity.ACTION_SELECT_FILES:
                if (resultCode == Activity.RESULT_OK) {
//                    if (UploadUtils.INSTANCE.getUploadState()) {
//                        NormalUtils.INSTANCE.showToast(ContainerActivity.this,"对不起,当前尚有未完成的上传任务，不能进行上传任务");
//                        return;
//                    }
                    try {
                        final JSONArray jsonArray = new JSONArray(data.getStringExtra("data"));
                        JSONObject object;
                        List<Integer> fileIds = new ArrayList<>();
                        for (int i = 0;i < jsonArray.length();i++) {
                            object = jsonArray.getJSONObject(i);
                            fileIds.add(object.getInt("file_id"));
                        }
                        Toast.makeText(this,"开始上传~",Toast.LENGTH_SHORT).show();
//                        loadingView.show(true);
                        checkUploadState();

                        UploadUtils.INSTANCE.uploadClassFiles(fileIds, curClass, new UploadUtils.UploadFilesListener() {
                            @Override
                            public void onUploadFilesFinished(List<FileObject> fileObjects) {
                                if (loadingView != null) {
                                    loadingView.show(false);
                                } else {
                                    return;
                                }

                                ClassObject classObject;
                                List<ClassObject> temp = DataSupport.where("classID=?",targetClass).find(ClassObject.class);
                                if (temp.size() > 0) {
                                    classObject = DataSupport.where("classID=?",targetClass).find(ClassObject.class).get(0);
                                } else {
                                    return;
                                }
                                List<ClassMemberObject> memberObjectList = classObject.getMemberList();
                                final List<String> targetIds = new ArrayList<>();
                                for (ClassMemberObject tempMember : memberObjectList) {
                                    targetIds.add(tempMember.getMemberID());
                                }

                                for (FileObject tempFile : fileObjects) {
                                    final JSONObject content = new JSONObject();
                                    try {
                                        content.put(MessageConst.MSG_TYPE, MessageConst.NEW_FILE_COMING);
                                        content.put(MessageConst.CONTENT_FROM, MainActivity.BASE_GLOBAL_DATA.getUserID());
                                        content.put(MessageConst.CONTENT_TARGET_ID, tempFile.getFileId());
                                        content.put(MessageConst.CONTENT_IN_CLASS, targetClass);
                                        content.put(MessageConst.CONTENT_FILE_NAME, tempFile.getName());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    DataWrapper wrapper = new DataWrapper(
                                            MainActivity.BASE_GLOBAL_DATA.getUserID(),
                                            targetClass,
                                            "",
                                            targetIds,
                                            content
                                    );

                                    int messageId = MessagePostUtil.INSTANCE.wannaPostMessage(wrapper);
                                    Log.e("TAG", "messageId = " + messageId);
                                }
                                //重载文件列表
                                titleView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        replaceFragment(FilesFragment.newInstance(targetClass), false);
                                    }
                                }, 1000);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this,"没有选择任何文件~",Toast.LENGTH_SHORT).show();
                }
                break;
            case AddGroupPopupWindow.ACTION_ADD_GROUP:
            case AddGroupPopupWindow.ACTION_ADD_NOTICE_GROUP:
                if (resultCode == RESULT_OK) {
                    String contactsStr = data.getStringExtra("contacts_data");
                    final String[] contacts = contactsStr.split("_");

                    //根据返回的成员组成名字
                    final List<UserObject> members = new ArrayList<>();
                    final StringBuilder nameStringBuilder = new StringBuilder();
                    for (String contactID : contacts) {
                        UserObject object = DataSupport.where("userID=?",contactID).find(UserObject.class).get(0);
                        members.add(object);
                        nameStringBuilder.append(object.getUserRealName());
                        nameStringBuilder.append("、");
                    }
                    nameStringBuilder.deleteCharAt(nameStringBuilder.length() - 1);

                    if (requestCode == AddGroupPopupWindow.ACTION_ADD_NOTICE_GROUP) {
                        NoticeGroupObject noticeGroupObject = new NoticeGroupObject();
                        noticeGroupObject.setName(nameStringBuilder.toString());
                        noticeGroupObject.setNoticeGroupID(new Date().getTime() + "");
                        ClassObject targetClassObject = DataSupport.where("classID=?",targetClass).find(ClassObject.class).get(0);
                        noticeGroupObject.setInClass(targetClassObject);
                        noticeGroupObject.save();

                        for (UserObject object : members) {
                            NoticeGroupMemberObject memberObject = new NoticeGroupMemberObject();
                            memberObject.setInNoticeGroup(noticeGroupObject);
                            memberObject.setMemberName(object.getUserRealName());
                            memberObject.setMemberID(object.getUserId());
                            memberObject.save();
                        }
                        RecentNoticeGroupMsg recentNoticeGroupMsg = new RecentNoticeGroupMsg();
                        recentNoticeGroupMsg.setContent("");
                        recentNoticeGroupMsg.setNoReadNumber(0);
                        recentNoticeGroupMsg.setTime(new Date());
                        recentNoticeGroupMsg.setNoticeGroup(noticeGroupObject);
                        recentNoticeGroupMsg.save();
                        Toast.makeText(ContainerActivity.this, "通知组创建完成，请尽快修改名字~", Toast.LENGTH_LONG).show();

                        titleView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //重新载入一次联系人列表
                                replaceFragment(ContactsFragment.newInstance(targetClass), false);
                            }
                        }, 1000);
                    } else {
                        Map<String, Object> attr = new HashMap<>();
                        attr.put("type", AppConst.ConversationType_Group);
                        attr.put("classId", targetClass);
                        CMApplication.imClient.createConversation(Arrays.asList(contacts), nameStringBuilder.toString(), attr,
                                new AVIMConversationCreatedCallback() {
                                    @Override
                                    public void done(final AVIMConversation avimConversation, AVException e) {
                                        if (e == null) {
                                            final SubClassObject subClassObject = new SubClassObject();
                                            subClassObject.setSubClassName(nameStringBuilder.toString());
                                            subClassObject.setTime(new Date());
                                            subClassObject.setSubClassID(avimConversation.getConversationId());
                                            final ClassObject targetClassObject = DataSupport.where("classID=?", targetClass).find(ClassObject.class).get(0);
                                            subClassObject.setInClass(targetClassObject);
                                            subClassObject.save();

                                            for (UserObject object : members) {
                                                SubClassMemberObject memberObject = new SubClassMemberObject();
                                                memberObject.setInSubClass(subClassObject);
                                                memberObject.setMemberName(object.getUserRealName());
                                                memberObject.setMemberID(object.getUserId());
                                                memberObject.save();
                                            }

                                            AVQuery<AVUser> query = AVUser.getQuery();
                                            query.getInBackground(MainActivity.BASE_GLOBAL_DATA.getUserID(), new GetCallback<AVUser>() {
                                                @Override
                                                public void done(AVUser object, AVException e) {
                                                    if (e == null) {
                                                        object.add("subClassIds", avimConversation.getConversationId());
                                                        object.saveInBackground();
                                                    } else {
                                                        NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT, e);
                                                    }
                                                }
                                            });

                                            titleView.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //重新载入一次联系人列表
                                                    replaceFragment(ContactsFragment.newInstance(targetClass), false);
                                                }
                                            }, 1000);
                                        } else {
                                            NormalUtils.INSTANCE.showError(ContainerActivity.this, e);
                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(ContainerActivity.this, "未选择任何联系人，创建未完成~", Toast.LENGTH_LONG).show();
                }
                break;
            case PhotosFragment.ACTION_UPLOAD_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
//                    if (UploadUtils.INSTANCE.getUploadState()) {
//                        NormalUtils.INSTANCE.showToast(ContainerActivity.this,"对不起,当前尚有未完成的上传任务，不能进行上传任务");
//                        return;
//                    }
                    String targetAlbumId = getIntent().getExtras().getString("target_album_id");
                    final int targetClassId = getIntent().getExtras().getInt("targetClass");
                    try {
                        final JSONArray picArray = new JSONArray(data.getStringExtra("data"));
                        final AlbumsObject targetAlbumObject = DataSupport.where("networkId=?",targetAlbumId).find(AlbumsObject.class).get(0);
                        List<Integer> photoIds = new ArrayList<>();
                        JSONObject object;
                        for (int i = 0;i < picArray.length();i++) {
                            object = picArray.getJSONObject(i);
                            photoIds.add(object.getInt("file_id"));
                        }

                        NormalUtils.INSTANCE.showToast(ContainerActivity.this, "开始上传~");
//                        loadingView.show(true);
                        checkUploadState();
                        UploadUtils.INSTANCE.uploadClassPhotos(photoIds, targetAlbumObject, new UploadUtils.UploadFilesListener() {
                            @Override
                            public void onUploadFilesFinished(List<FileObject> fileIds) {
                                if (loadingView != null) {
                                    loadingView.show(false);
                                } else {
                                    return;
                                }

                                replaceFragment(PhotosFragment.newInstance(targetAlbumObject.getNetworkId(),targetClassId), false);
                                NormalUtils.INSTANCE.showToast(ContainerActivity.this, "上传完成~");
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(ContainerActivity.this,"没有选择任何图片~",Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
    }

    /*设置backView的动作*/
    public interface OnBackListener {
        void onBack();
    }

    public void setOnBackListener(OnBackListener backListener) {
        this.backListener = backListener;
    }

    /*设置标题栏*/
    public void setTitle(String title) {
        titleView.setText(title);
    }

    private List<Integer> targetActionView;

    /*显示指定的actionViews*/
    public void showActionViews(List<Integer> viewsId) {

        this.targetActionView = viewsId;

        for (Integer i : actionViews) {
            findViewById(i).setVisibility(View.GONE);
        }

        if (viewsId != null) {
            for (Integer i2 : viewsId) {
                findViewById(i2).setVisibility(View.VISIBLE);
            }
        }
    }

    //记录当前activity是否在前台
    private boolean isFrontInScreen = true;

    //如果当前后台存在下载进度，不允许点击上传
    private void checkUploadState() {
        if (UploadUtils.INSTANCE.getUploadState()) {
            uploadReceiver = new UploadFinishReceiver();
            registerReceiver(uploadReceiver,new IntentFilter("cm.action.upload_finished"));
            findViewById(R.id.main_container_upload_photos).setVisibility(View.GONE);
            findViewById(R.id.main_container_upload_file).setVisibility(View.GONE);

            ProgressBarCircularIndeterminate progressBar = (ProgressBarCircularIndeterminate)findViewById(R.id.main_container_upload_progress);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    //处理当前有下载过程的情况
    private class UploadFinishReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            findViewById(R.id.main_container_upload_progress).setVisibility(View.GONE);
            if (targetActionView != null) {
                for (Integer i2 : targetActionView) {
                    findViewById(i2).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private UploadFinishReceiver uploadReceiver = null;

    @Override
    protected void onStop() {
        super.onStop();
        if (uploadReceiver != null) {
            unregisterReceiver(uploadReceiver);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFrontInScreen = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isFrontInScreen = true;
    }

    /*动态切换Fragment*/
    public void replaceFragment(BaseFragment newFragment,boolean isToBackStack) {
        if (!isFrontInScreen) {
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out, R.anim.push_left_in, R.anim.push_left_out);
        fragmentTransaction.replace(R.id.fragment_container_contentView, newFragment);

        if (isToBackStack) {
            fragmentTransaction.addToBackStack(null);//可以把碎片添加活动中后，用户可以单击Back按钮移除它，显示前一个碎片
        }
        fragmentTransaction.commit();
    }

    /*动态切换Fragment*/
    public void replaceFragmentNoAnimation(BaseFragment newFragment,boolean isToBackStack) {
        if (!isFrontInScreen) {
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_contentView,newFragment);

        if (isToBackStack) {
            fragmentTransaction.addToBackStack(null);//可以把碎片添加活动中后，用户可以单击Back按钮移除它，显示前一个碎片
        }
        fragmentTransaction.commit();
    }
}
