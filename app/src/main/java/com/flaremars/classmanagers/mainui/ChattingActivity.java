package com.flaremars.classmanagers.mainui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.GetFileCallback;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMFileMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.FileObject;
import com.flaremars.classmanagers.model.FileType;
import com.flaremars.classmanagers.model.FragmentConst;
import com.flaremars.classmanagers.model.LocalFileObject;
import com.flaremars.classmanagers.model.MessageObject;
import com.flaremars.classmanagers.model.MsgType;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.SubClassMemberObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.service.CMService;
import com.flaremars.classmanagers.uis.BrowsePhotoActivity;
import com.flaremars.classmanagers.uis.ContainerActivity;
import com.flaremars.classmanagers.uis.DownloadProgressDialog;
import com.flaremars.classmanagers.uis.SingleExcelActivity;
import com.flaremars.classmanagers.utils.BitmapUtils;
import com.flaremars.classmanagers.utils.DisplayUtils;
import com.flaremars.classmanagers.utils.FileUtils;
import com.flaremars.classmanagers.utils.FlareBitmapUtils;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.utils.NotificationUtils;
import com.flaremars.classmanagers.views.Dialog;
import com.flaremars.classmanagers.views.EmojiSelector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChattingActivity extends FragmentActivity {

    private static final int MAX_SIZE_ON_CREATE = 20;//初始化时最多刷出的数目

    private static final int SIZE_PER_REFRESH = 10;//但数据量大于上值时，每次更新显示十条

    private static final int ACTION_CAMERA = 1;

    private static final int ACTION_GET_EXCEL_MODEL = 4;

    private static Bitmap mineHeaderImg;

    private List<MessageObject> items;

    private MyAdapter adapter;

    private EditText inputEditText;

    private ListView listView;

    private ImageView emotionIvBtn;

    private LinearLayout inputContainer;

    //实际数据相关
    private int target;

    private SubClassObject targetSubClass;

    private List<UserObject> targetUsers;

    private ClassObject targetClass;

    private UserObject mine;

    private boolean isGroup;

    //拍照相关
    private String tempName;
    private String tempPath;

    public static int messageBoxMaxWidth = 230;

    public static int padding2dp = 2;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);

    private MessageReceiver messageReceiver;

    //界面模式切换必须参数
    private int stateBarHeight = 0;

    private int topBarHeight = 135;

    private int secondTopBarHeight = 45;

    private int emotionSelectorHeight = 10;

    private int inputLayoutHeight = 0;

    private boolean isEmojiIvBtnAction = false;

    private boolean isClickInput = false;

    /*
    private int screenHeight = 0;

    private int screenWidth = 0;
    */

    //默认状态，输入法不弹出，表情选择平台不弹出
    public static final int INPUT_MODE_NONE = 0;

    //表情选择平台弹出
    public static final int INPUT_MODE_EMOTION = 1;

    //输入法弹出
    public static final int INPUT_MODE_SOFT = 2;

    private int inputMode = INPUT_MODE_NONE;

    private View headerPaddingView;

    private View footerPaddingView;

    public static final Map<String,AVIMConversation> CacheIMConversation = new HashMap<>(10);

    private String targetConversation;

    private AVIMConversation curConversation;

    private Set<AVIMTypedMessage> messageBuffer = new HashSet<>();

    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

    //展示、缩放动画
    private AnimatorSet showSmallBar;

    private AnimatorSet showFullBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        listView = (ListView) findViewById(R.id.lv_chatting_content);
        inputEditText = (EditText) findViewById(R.id.et_chatting_input);
        TextView sendTvBtn = (TextView) findViewById(R.id.tv_btn_chatting_send);
        emotionIvBtn = (ImageView) findViewById(R.id.iv_btn_chatting_emotion_or_keyboard);
        LinearLayout secondTopBar = (LinearLayout) findViewById(R.id.ll_chatting_top_bar_second);
        TextView firstTitleName = (TextView) findViewById(R.id.tv_chatting_header_name);
        TextView secondTitleName = (TextView) findViewById(R.id.tv_chatting_header_name_second);
        ImageView headerImgView = (ImageView) findViewById(R.id.iv_chatting_header);
        EmojiSelector emojiSelector = (EmojiSelector) findViewById(R.id.chatting_emojiselector);
        LinearLayout fullBar = (LinearLayout) findViewById(R.id.ll_chatting_header_container);
        inputContainer = (LinearLayout) findViewById(R.id.ll_input_container);
        final SwipeRefreshLayout contentContainer = (SwipeRefreshLayout) findViewById(R.id.srl_chatting_content_container);

        //初始化必要UI参数
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;

        messageBoxMaxWidth = screenWidth - DisplayUtils.INSTANCE.dp2px(this,90);
        padding2dp = DisplayUtils.INSTANCE.dp2px(this,2);

        Rect frame = new Rect();
        getWindow().getDecorView()
                .getWindowVisibleDisplayFrame(frame);
        stateBarHeight = frame.top;

        topBarHeight = DisplayUtils.INSTANCE.dp2px(this,topBarHeight) + DisplayUtils.INSTANCE.sp2px(this,14);

        secondTopBarHeight = DisplayUtils.INSTANCE.dp2px(this,secondTopBarHeight);

        emotionSelectorHeight = DisplayUtils.INSTANCE.dp2px(this,emotionSelectorHeight);
        int EMOJI_SIZE = screenWidth / 7;
        emotionSelectorHeight += 3 * EMOJI_SIZE;

        inputLayoutHeight = DisplayUtils.INSTANCE.dp2px(this,30) + DisplayUtils.INSTANCE.sp2px(this,14);


        //初始化动画
        showSmallBar =  (new AnimatorSet()).setDuration(300L);
        showFullBar = (new AnimatorSet()).setDuration(300L);
        OvershootInterpolator interpolator = new OvershootInterpolator();

        ObjectAnimator showSmallHideFullForFullBar = ObjectAnimator.ofFloat(fullBar, "translationY", 0.0f, -topBarHeight);
        ObjectAnimator showSmallHideFullForSmallBar = ObjectAnimator.ofFloat(secondTopBar, "translationY", 0.0f, secondTopBarHeight);
        ObjectAnimator showFullHideSmallForFullBar = ObjectAnimator.ofFloat(fullBar, "translationY", -topBarHeight, 0.0f);
        ObjectAnimator showFullHideSmallForSmallBar = ObjectAnimator.ofFloat(secondTopBar, "translationY", secondTopBarHeight, 0.0f);
        showFullBar.setInterpolator(interpolator);
        showFullBar.playSequentially(showFullHideSmallForSmallBar, showFullHideSmallForFullBar);
        showSmallBar.setInterpolator(interpolator);
        showSmallBar.playSequentially(showSmallHideFullForFullBar,showSmallHideFullForSmallBar);

        /*聊天数据分页相关*/
        contentContainer.setProgressViewEndTarget(false, topBarHeight + 180);
        contentContainer.setColorSchemeColors(Color.BLUE, Color.RED, Color.GREEN, Color.CYAN);

        target = getIntent().getIntExtra("target", -1);
        isGroup = getIntent().getBooleanExtra("is_group", false);

        //初始化聊天必须数据对象
        targetUsers = new ArrayList<>();
        if (target != -1) {
            if (isGroup) {
                targetSubClass = DataSupport.find(SubClassObject.class, target);
                List<SubClassMemberObject> tempList = targetSubClass.getMemberList();
                for (SubClassMemberObject member : tempList) {
                    UserObject userObject = DataSupport.where("userID=?", member.getMemberID()).find(UserObject.class).get(0);
                    targetUsers.add(userObject);
                }
                headerImgView.setImageResource(R.drawable.default_ic_group);
            } else {
                UserObject targetUser = DataSupport.find(UserObject.class, target);
                targetUsers.add(targetUser);
                final String imgPath = targetUser.getImgId();
                if (imgPath == null || imgPath.equals("")) {
                    headerImgView.setImageResource(R.drawable.default_ic_contact);
                } else {
                    FlareBitmapUtils.INSTANCE.loadBitmap(headerImgView, imgPath);
                }

                //如果是单聊模式，点击头像，可以查看大图
                headerImgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChattingActivity.this, BrowsePhotoActivity.class);
                        intent.putExtra("targetPhoto", imgPath);
                        startActivity(intent);
                    }
                });
            }
        }

        String classId = getIntent().getStringExtra("targetclass_id");
        if (classId == null) {
            classId = MainActivity.BASE_GLOBAL_DATA.getCurClassID();
        }
        targetClass = DataSupport.where("classID=?",classId).
                find(ClassObject.class).get(0);
        mine = DataSupport.where("userId=?",MainActivity.BASE_GLOBAL_DATA.getUserID()).find(UserObject.class).get(0);

        /*初始化会话对象*/
        if (!isGroup) {
            final UserObject targetUser = DataSupport.find(UserObject.class, target);
            String conversationId = targetUser.getConversationId();
            if (conversationId == null || conversationId.equals("")) {

                final List<String> members = new ArrayList<>();
                members.add(mine.getUserId());
                members.add(targetUser.getUserId());

                AVIMConversationQuery conversationQuery = CMApplication.imClient.getQuery();
                conversationQuery.containsMembers(members);
                conversationQuery.whereEqualTo("attr.type", AppConst.ConversationType_OneOne);
                conversationQuery.findInBackground(new AVIMConversationQueryCallback() {
                    @Override
                    public void done(List<AVIMConversation> list, AVException e) {
                        if (e == null) {
                            if (list.size() > 0) {
                                AVIMConversation avimConversation = list.get(0);
                                curConversation = avimConversation;
                                CacheIMConversation.put(avimConversation.getConversationId(), avimConversation);

                                targetUser.setConversationId(avimConversation.getConversationId());
                                targetUser.update(targetUser.getId());
                            } else {
                                Map<String, Object> attr = new HashMap<>();
                                attr.put("type", AppConst.ConversationType_OneOne);
                                attr.put("classId", targetClass.getClassID());
                                CMApplication.imClient.createConversation(members, members.get(0) + "_" + members.get(1), attr, new AVIMConversationCreatedCallback() {
                                    @Override
                                    public void done(AVIMConversation avimConversation, AVException e) {
                                        if (e == null) {
                                            Log.e("TAG", "成功创建单人会话");
                                            curConversation = avimConversation;
                                            CacheIMConversation.put(avimConversation.getConversationId(), avimConversation);

                                            targetUser.setConversationId(avimConversation.getConversationId());
                                            targetUser.update(targetUser.getId());
                                        } else {
                                            Toast.makeText(ChattingActivity.this,"当前网络状态不佳",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(ChattingActivity.this,"当前网络状态不佳",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                targetConversation = conversationId;
            }
        } else {
            targetConversation = getIntent().getStringExtra("target_conversation");
        }

        if (targetConversation != null) {
            curConversation = CacheIMConversation.get(targetConversation);
            //不在缓存里面的话，就从网上获取
            if (curConversation == null) {
                AVIMConversationQuery query1 = CMApplication.imClient.getQuery();
                query1.whereEqualTo("objectId", targetConversation);
                query1.findInBackground(new AVIMConversationQueryCallback() {
                    @Override
                    public void done(List<AVIMConversation> list, AVException e) {
                        if (e == null) {
                            if (list.size() > 0) {
                                curConversation = list.get(0);
                                CacheIMConversation.put(targetConversation, curConversation);
                            } else {
                                Log.e("TAG", "意料外的错误");
                            }
                        } else {
                            NormalUtils.INSTANCE.showError(ChattingActivity.this, e);
                            Log.e("TAG", "不能发送数据");
                        }
                    }
                });
            }
        }

        NotificationUtils.INSTANCE.setMode(0);

        //初始化聊天界面
        inputEditText.requestFocus();
        if (mine.getImgId().equals("")) {
            mineHeaderImg = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_contact);
        } else {
            mineHeaderImg = FlareBitmapUtils.INSTANCE.getBitmapFromCache(mine.getImgId(), 150, 150);
        }
        int size;
        if (targetSubClass != null) {
            firstTitleName.setText(targetSubClass.getSubClassName());
            secondTitleName.setText(targetSubClass.getSubClassName());

            size = DataSupport.where("subclassobject_id=? and ownerId=?",
                    String.valueOf(targetSubClass.getId()),MainActivity.BASE_GLOBAL_DATA.getUserID()).
                    count(MessageObject.class);
            if (size > MAX_SIZE_ON_CREATE) {
                items = DataSupport.where("subclassobject_id=? and ownerId=?",
                        String.valueOf(targetSubClass.getId()),MainActivity.BASE_GLOBAL_DATA.getUserID()).
                        offset(size - MAX_SIZE_ON_CREATE).
                        limit(MAX_SIZE_ON_CREATE).
                        find(MessageObject.class, true);
            } else {
                items = DataSupport.where("subclassobject_id=? and ownerId=?",
                        String.valueOf(targetSubClass.getId()),MainActivity.BASE_GLOBAL_DATA.getUserID()).
                        find(MessageObject.class, true);
            }
        } else {
            firstTitleName.setText(targetUsers.get(0).getUserRealName());
            secondTitleName.setText(targetUsers.get(0).getUserRealName());

            size = DataSupport.where("userobject_id=?  and subclassobject_id is null and ownerId=?",
                    String.valueOf(targetUsers.get(0).getId()),MainActivity.BASE_GLOBAL_DATA.getUserID()).
                    count(MessageObject.class);
            if (size > MAX_SIZE_ON_CREATE) {
                items = DataSupport.where("userobject_id=?  and subclassobject_id is null and ownerId=?",
                        String.valueOf(targetUsers.get(0).getId()),MainActivity.BASE_GLOBAL_DATA.getUserID()).
                        offset(size - MAX_SIZE_ON_CREATE).
                        limit(MAX_SIZE_ON_CREATE).
                        find(MessageObject.class,true);
            } else {
                items = DataSupport.where("userobject_id=?  and subclassobject_id is null and ownerId=?",
                        String.valueOf(targetUsers.get(0).getId()),MainActivity.BASE_GLOBAL_DATA.getUserID()).
                        find(MessageObject.class, true);
            }
        }
        //设置inputContainer的margin
        FrameLayout.LayoutParams inputContainerLayoutParams = new FrameLayout.LayoutParams(inputContainer.getLayoutParams());
        inputContainerLayoutParams.bottomMargin = -emotionSelectorHeight;
        inputContainerLayoutParams.gravity = Gravity.BOTTOM;
        inputContainer.setLayoutParams(inputContainerLayoutParams);

        headerPaddingView = new View(this);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                topBarHeight + stateBarHeight);
        headerPaddingView.setLayoutParams(lp);
        headerPaddingView.setClickable(true);
        headerPaddingView.setBackgroundColor(Color.TRANSPARENT);
        listView.addHeaderView(headerPaddingView);

        footerPaddingView = new View(this);
        lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                inputLayoutHeight);
        footerPaddingView.setLayoutParams(lp);
        footerPaddingView.setClickable(true);
        footerPaddingView.setBackgroundColor(Color.TRANSPARENT);
        listView.addFooterView(footerPaddingView);

        adapter = new MyAdapter(this,items);
        emojiSelector.setBinder(inputEditText);

        sendTvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = inputEditText.getText().toString();
                if (!msg.equals("")) {
                    MessageObject messageObject = new MessageObject();
                    messageObject.setContent("normal:" + msg);
                    messageObject.setTime(new Date());
                    messageObject.setRead(true);
                    if (targetSubClass != null) {
                        messageObject.setInSubClassObject(targetSubClass);
                        messageObject.setInUserObject(mine);
                        messageObject.setType(MsgType.NORMAL_MINE.ordinal());

                        updateRecentMessage(RecentMessageObject.TYPE_GROUP, msg, targetSubClass.getId());
                    } else {
                        messageObject.setInUserObject(targetUsers.get(0));
                        messageObject.setType(MsgType.NORMAL_MINE.ordinal());

                        updateRecentMessage(RecentMessageObject.TYPE_PERSON, msg, targetUsers.get(0).getId());
                    }
                    messageObject.setOwnerId(MainActivity.BASE_GLOBAL_DATA.getUserID());
                    messageObject.save();
                    items.add(messageObject);
                    adapter.notifyDataSetChanged();
                    inputEditText.setText("");
                    listView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listView.smoothScrollToPosition(items.size() + 1);
                        }
                    },250);

                    new SendingMessageTask().execute("normal:"+msg);
                }
            }
        });

        //表情小按钮的点击响应
        emotionIvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEmojiIvBtnAction = true;
                switch (inputMode) {
                    case INPUT_MODE_SOFT:
                        uiChangeSoft2Emotion();
                        break;
                    case INPUT_MODE_NONE:
                        uiChangeNone2Emotion();
                        break;
                    case INPUT_MODE_EMOTION:
                        uiChangeEmotion2Soft(true);
                        break;
                    default:
                }
            }
        });

        //通过点击listview 关闭处于打开状态的输入法，以及将表情选择器隐藏
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (inputMode == INPUT_MODE_EMOTION) {
                        uiChangeEmotion2None();
                    } else if (inputMode == INPUT_MODE_SOFT) {
                        uiChangeSoft2None(true);
                    }
                }
                return false;
            }
        });

        //点用于点击输入框的时候认为其需要弹出输入法
        inputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputMode == INPUT_MODE_EMOTION) {
                    isClickInput = true;
                    uiChangeEmotion2Soft(false);
                } else if (inputMode == INPUT_MODE_NONE) {
                    uiChangeNone2Soft();
                }
            }
        });

        //主要用于监控输入法的出现与隐藏
        inputContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {

                if (bottom > oldBottom && oldBottom != 0) {
                    //当用户直接点击输入法的收回按键
                    if (isEmojiIvBtnAction) {
                        isEmojiIvBtnAction = false;
                        return;
                    }

                    if (inputMode == INPUT_MODE_SOFT && !isClickInput) {
                        isEmojiIvBtnAction = false;
                        uiChangeSoft2None(false);
                    }

                    isClickInput = false;
                }
            }
        });


        findViewById(R.id.iv_btn_chatting_back_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChattingActivity.this.finish();
            }
        });

        findViewById(R.id.iv_btn_chatting_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChattingActivity.this.finish();
            }
        });

        //功能栏接口设置，群聊时取消 ”消息录制“
            if (isGroup) {
                findViewById(R.id.iv_btn_chatting_excel).setVisibility(View.GONE);
            } else {
                findViewById(R.id.iv_btn_chatting_excel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChattingActivity.this, ContainerActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_EXCEL_MODELS);
                        bundle.putString(AppConst.TARGET_NOTICE_GROUP, String.valueOf(target));
                        bundle.putBoolean(AppConst.IS_POINT_TO_PERSON, true);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, ACTION_GET_EXCEL_MODEL);
                    }
                });
            }

            findViewById(R.id.iv_btn_chatting_send_file).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChattingActivity.this, SelectFilesActivity.class);
                    intent.putExtra("file_select_mode", SelectFilesActivity.FILE_SELECT_MODE.ONLY_DOCUMENT);
                    startActivityForResult(intent, SelectFilesActivity.ACTION_SELECT_FILES);
                }
            });

            findViewById(R.id.iv_btn_chatting_send_picture).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChattingActivity.this, LocalPicturesActivity.class);
                    startActivityForResult(intent, SelectFilesActivity.ACTION_SELECT_PHOTO);
                }
            });

            findViewById(R.id.iv_btn_chatting_take_photo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File storagePath = FileUtils.getInstance().getPhotosDir();
                    Date date = new Date();
                    tempName = dateFormat.format(date) + ".jpg";
                    String filePath = storagePath.getPath() + File.separator + tempName;
                    tempPath = filePath;
                    File file = new File(filePath);
                    Intent intentCamera = new Intent("android.media.action.IMAGE_CAPTURE");
                    intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    intentCamera.putExtra("return-data", true);
                    startActivityForResult(intentCamera, ACTION_CAMERA);
                }
            });

        //设置聊天数据刷新监视器
        contentContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int curSize = items.size();
                if (targetSubClass != null) {
                    int totalSize = DataSupport.where("subclassobject_id=? and ownerId=?",
                            String.valueOf(targetSubClass.getId()),MainActivity.BASE_GLOBAL_DATA.getUserID()).
                            count(MessageObject.class);
                    int newSize = totalSize - curSize;
                    newSize = (newSize > 10) ? SIZE_PER_REFRESH : newSize;
                    List<MessageObject> newItems = DataSupport.where("subclassobject_id=? and ownerId=?",
                            String.valueOf(targetSubClass.getId()),MainActivity.BASE_GLOBAL_DATA.getUserID()).
                            offset(totalSize - curSize - newSize).
                            limit(newSize).
                            find(MessageObject.class,true);
                    curSize = newSize;
                    items.addAll(0,newItems);
                } else {
                    int totalSize = DataSupport.where("userobject_id=?  and subclassobject_id is null and ownerId=?",
                            String.valueOf(targetUsers.get(0).getId()),MainActivity.BASE_GLOBAL_DATA.getUserID()).
                            count(MessageObject.class);

                    int newSize = totalSize - curSize;
                    newSize = (newSize > 10) ? SIZE_PER_REFRESH : newSize;
                    List<MessageObject> newItems = DataSupport.where("userobject_id=?  and subclassobject_id is null and ownerId=?",
                            String.valueOf(targetUsers.get(0).getId()),MainActivity.BASE_GLOBAL_DATA.getUserID()).
                            offset(totalSize - curSize - newSize).
                            limit(newSize).
                            find(MessageObject.class, true);
                    curSize = newSize;
                    items.addAll(0, newItems);
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(curSize - 1);
                contentContainer.setRefreshing(false);
            }
        });

        listView.setAdapter(adapter);
        //界面显示的瞬间将内容移到最后的一项
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(items.size() + 1);
            }
        },1);

        //定时重发buffer数据
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (messageBuffer.size() > 0) {
                    if (curConversation != null) {
                        for (AVIMTypedMessage msg : messageBuffer) {
                            curConversation.sendMessage(msg, new AVIMConversationCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e != null) {
                                        Log.e("TAG",e.getMessage() + " " + e.getCode());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        },0,3000, TimeUnit.MILLISECONDS);
    }

    //INPUT_MODE_NONE -> INPUT_MODE_EMOTION
    private void uiChangeNone2Emotion() {
//        Log.e("TAG","uiChangeNone2Emotion");
        showSmallBar.start();
        showFullBar.cancel();

        FrameLayout.LayoutParams inputContainerLayoutParams = new FrameLayout.LayoutParams(inputContainer.getLayoutParams());
        inputContainerLayoutParams.bottomMargin = 0;
        inputContainerLayoutParams.gravity = Gravity.BOTTOM;
        inputContainer.setLayoutParams(inputContainerLayoutParams);

        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                secondTopBarHeight + stateBarHeight);
        headerPaddingView.setLayoutParams(lp);
        lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                emotionSelectorHeight + inputLayoutHeight);
        footerPaddingView.setLayoutParams(lp);

        listView.setSelection(items.size());

        //转为小键盘图标
        emotionIvBtn.setImageResource(R.drawable.ic_textin_keyboard);

        //记录最新状态
        inputMode = INPUT_MODE_EMOTION;
    }

    //INPUT_MODE_EMOTION -> INPUT_MODE_NONE
    private void uiChangeEmotion2None() {
//        Log.e("TAG","uiChangeEmotion2None");
        showSmallBar.cancel();
        showFullBar.start();

        FrameLayout.LayoutParams inputContainerLayoutParams = new FrameLayout.LayoutParams(inputContainer.getLayoutParams());
        inputContainerLayoutParams.bottomMargin = -emotionSelectorHeight;
        inputContainerLayoutParams.gravity = Gravity.BOTTOM;
        inputContainer.setLayoutParams(inputContainerLayoutParams);

        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                topBarHeight + stateBarHeight);
        headerPaddingView.setLayoutParams(lp);
        lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                inputLayoutHeight);
        footerPaddingView.setLayoutParams(lp);

        listView.setSelection(items.size());

        //转为表情图标
        emotionIvBtn.setImageResource(R.mipmap.ic_textin_emotion);

        //记录最新状态
        inputMode = INPUT_MODE_NONE;
    }

    //INPUT_MODE_NONE -> INPUT_MODE_SOFT
    private void uiChangeNone2Soft() {
//        Log.e("TAG","uiChangeNone2Soft");
        showSmallBar.start();
        showFullBar.cancel();

        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                secondTopBarHeight + stateBarHeight);
        headerPaddingView.setLayoutParams(lp);

        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(items.size());
            }
        },150);
        //记录最新状态
        inputMode = INPUT_MODE_SOFT;
    }


    //INPUT_MODE_SOFT -> INPUT_MODE_NONE
    private void uiChangeSoft2None(boolean isHandledClose) {
//        Log.e("TAG","uiChangeSoft2None");
        showSmallBar.cancel();
        showFullBar.start();

        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                topBarHeight + stateBarHeight);
        headerPaddingView.setLayoutParams(lp);

        if (isHandledClose) {
            closeSoftInput();
        }

        //记录最新状态
        inputMode = INPUT_MODE_NONE;
    }

    //INPUT_MODE_EMOTION -> INPUT_MODE_SOFT
    private void uiChangeEmotion2Soft(boolean isHandledOpen) {
//        Log.e("TAG","uiChangeEmotion2Soft");

        if (isHandledOpen) {
            openSoftInput();
        }

        FrameLayout.LayoutParams inputContainerLayoutParams = new FrameLayout.LayoutParams(inputContainer.getLayoutParams());
        inputContainerLayoutParams.bottomMargin = -emotionSelectorHeight;
        inputContainerLayoutParams.gravity = Gravity.BOTTOM;
        inputContainer.setLayoutParams(inputContainerLayoutParams);

        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                inputLayoutHeight);
        footerPaddingView.setLayoutParams(lp);

        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(items.size());
            }
        }, 150);

        emotionIvBtn.setImageResource(R.mipmap.ic_textin_emotion);

        inputMode = INPUT_MODE_SOFT;
    }

    //INPUT_MODE_SOFT -> INPUT_MODE_EMOTION
    private void uiChangeSoft2Emotion() {
//        Log.e("TAG","uiChangeSoft2Emotion");

        closeSoftInput();
        inputContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams inputContainerLayoutParams = new FrameLayout.LayoutParams(inputContainer.getLayoutParams());
                inputContainerLayoutParams.bottomMargin = 0;
                inputContainerLayoutParams.gravity = Gravity.BOTTOM;
                inputContainer.setLayoutParams(inputContainerLayoutParams);
            }
        }, 250);

        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                emotionSelectorHeight + inputLayoutHeight);
        footerPaddingView.setLayoutParams(lp);

        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(items.size());
            }
        }, 250);

        emotionIvBtn.setImageResource(R.drawable.ic_textin_keyboard);

        inputMode = INPUT_MODE_EMOTION;
    }


    //关闭输入法
    private void closeSoftInput() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //打开输入法
    private void openSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //发送聊天数据
    private void sendMessageToTarget(String content,MsgType type) {

        int intType = type.ordinal();
        String inClass = targetClass.getClassID();
        AVIMTextMessage message = new AVIMTextMessage();
        message.setText(content.split(":")[1]);
        Map<String,Object> attrs = new HashMap<>();
        attrs.put("type",intType);
        attrs.put("inClass",inClass);
        message.setAttrs(attrs);
        if (curConversation != null) {
            curConversation.sendMessage(message, new AVIMConversationCallback() {
                @Override
                public void done(AVException e) {
                    if (e != null) {
                        NormalUtils.INSTANCE.showError(ChattingActivity.this, e);
                    }
                }
            });
        } else {
            messageBuffer.add(message);
        }
    }

    //后台发出数据
    private class SendingMessageTask extends AsyncTask<String,Void,Void> {

        @Override
        protected Void doInBackground(String... params) {
            sendMessageToTarget(params[0],MsgType.NORMAL_OTHERS);
            return null;
        }

    }

    //接受 获取消息服务的广播
    private class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String senderId;
            if (targetUsers.size() > 1) {
                senderId = targetSubClass.getSubClassID();
                List<RecentMessageObject> recentMessageObjects = DataSupport.where("targetId=? and type=? and classobject_id=? and userobject_id=?",
                        String.valueOf(targetSubClass.getId()),
                        String.valueOf(RecentMessageObject.TYPE_GROUP),
                        String.valueOf(targetClass.getId()),
                        String.valueOf(mine.getId())).find(RecentMessageObject.class);
                RecentMessageObject recentMessageObject;
                if (recentMessageObjects.size() > 0) {
                    recentMessageObject = recentMessageObjects.get(0);
                } else {
                    return;
                }
                recentMessageObject.setToDefault("noReadNumber");
                recentMessageObject.update(recentMessageObject.getId());
            } else {
                senderId = targetUsers.get(0).getUserId();
                List<RecentMessageObject> recentMessageObjects = DataSupport.where("targetId=? and type=? and classobject_id=? and userobject_id=?",
                        String.valueOf(targetUsers.get(0).getId()),
                        String.valueOf(RecentMessageObject.TYPE_PERSON),
                        String.valueOf(targetClass.getId()),
                        String.valueOf(mine.getId())).find(RecentMessageObject.class);
                RecentMessageObject recentMessageObject;
                if (recentMessageObjects.size() > 0) {
                    recentMessageObject = recentMessageObjects.get(0);
                } else {
                    return;
                }
                recentMessageObject.setToDefault("noReadNumber");
                recentMessageObject.update(recentMessageObject.getId());
            }


            if (!senderId.equals(intent.getStringExtra("message_sender"))) {
                return;
            }
            MessageObject newMessage = DataSupport.find(MessageObject.class,intent.getIntExtra("message_id",-1),true);
            if (newMessage == null) {
                return;
            }

            items.add(newMessage);
            adapter.notifyDataSetChanged();
            listView.smoothScrollToPosition(items.size() + 1);
        }
    }

    //写入最近聊天记录
    private void updateRecentMessage(int targetType,String msg,int id) {
        List<RecentMessageObject> recentMessageObjects = DataSupport.where("targetID=? and type=? and classobject_id=? and userobject_id=?", String.valueOf(id),
                String.valueOf(targetType),String.valueOf(targetClass.getId()),String.valueOf(mine.getId())).
                find(RecentMessageObject.class);

        if (targetType == RecentMessageObject.TYPE_GROUP) {
            msg = mine.getUserRealName() + " : " + msg;
        }

        if (recentMessageObjects.size() == 0) {
            RecentMessageObject recentMessageObject = new RecentMessageObject();
            recentMessageObject.setInClass(targetClass);
            recentMessageObject.setNoReadNumber(0);
            recentMessageObject.setContent(msg);
            String name;
            if (targetType == RecentMessageObject.TYPE_GROUP) {
                name = targetSubClass.getSubClassName();
                recentMessageObject.setImgPath("");
            } else {
                name = targetUsers.get(0).getUserRealName();
                recentMessageObject.setImgPath(targetUsers.get(0).getImgId());
            }
            recentMessageObject.setName(name);
            recentMessageObject.setTime(new Date());
            recentMessageObject.setType(targetType);
            recentMessageObject.setTargetID(id);
            recentMessageObject.setOwner(mine);
            recentMessageObject.save();
        } else {
            RecentMessageObject recentMessageObject = recentMessageObjects.get(0);
            recentMessageObject.setNoReadNumber(0);
            recentMessageObject.setTime(new Date());
            recentMessageObject.setContent(msg);
            recentMessageObject.update(recentMessageObject.getId());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        messageReceiver = new MessageReceiver();
        IntentFilter intentFilter = new IntentFilter(CMService.HAS_NEW_MESSAGES);
        registerReceiver(messageReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(messageReceiver);
    }

    private void addChattingItem(String content,String recentMessageFlag,MsgType type) {
        MessageObject messageObject = new MessageObject();
        messageObject.setContent(content);
        messageObject.setTime(new Date());
        messageObject.setRead(true);
        if (targetSubClass != null) {
            messageObject.setInSubClassObject(targetSubClass);
            messageObject.setInUserObject(mine);
            messageObject.setType(type.ordinal());

            updateRecentMessage(RecentMessageObject.TYPE_GROUP,recentMessageFlag,targetSubClass.getId());
        } else {
            messageObject.setInUserObject(targetUsers.get(0));
            messageObject.setType(type.ordinal());

            updateRecentMessage(RecentMessageObject.TYPE_PERSON, recentMessageFlag, targetUsers.get(0).getId());
        }
        messageObject.setOwnerId(mine.getUserId());
        messageObject.save();

        items.add(messageObject);
        inputEditText.setText("");
        adapter.notifyDataSetChanged();
        listView.smoothScrollToPosition(items.size());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scheduledThreadPool != null && !scheduledThreadPool.isTerminated()) {
            scheduledThreadPool.shutdown();
        }
        NotificationUtils.INSTANCE.setMode(1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!NormalUtils.INSTANCE.isNetworkRegularWork(ChattingActivity.this)) {
            NormalUtils.INSTANCE.showToast(ChattingActivity.this,"当前网络连接不佳，请稍候重试");
            return;
        }
        switch (requestCode) {
            case ACTION_CAMERA:
                if (resultCode == RESULT_OK) {
                    addChattingItem("picture:" + tempPath, "[图片]", MsgType.NORMAL_MINE);

                    int intType = MsgType.NORMAL_OTHERS.ordinal();
                    String inClass = targetClass.getClassID();
                    AVIMImageMessage message = null;
                    try {
                        String targetPath = BitmapUtils.INSTANCE.getLocalThumbnail(tempPath);
                        AVFile imgFile = AVFile.withAbsoluteLocalPath(new File(tempPath).getName(),targetPath);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(tempPath,options);
                        imgFile.addMetaData("width", options.outWidth);
                        imgFile.addMetaData("height",options.outHeight);
                        message = new AVIMImageMessage(imgFile);
                        Map<String,Object> attrs = new HashMap<>();
                        attrs.put("type",intType);
                        attrs.put("inClass",inClass);
                        message.setAttrs(attrs);
//                        message.setProgressCallback(new ProgressCallback() {
//                            @Override
//                            public void done(Integer integer) {
//                                Log.e("Uploading",integer + "");
//                            }
//                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (curConversation == null) {
                        messageBuffer.add(message);
                        NormalUtils.INSTANCE.showToast(ChattingActivity.this, "网络状态不佳");
                    } else {
                        curConversation.sendMessage(message, new AVIMConversationCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e != null) {
                                    NormalUtils.INSTANCE.showError(ChattingActivity.this, e);
                                }
                            }
                        });
                    }
                }
                break;
            case SelectFilesActivity.ACTION_SELECT_FILES:
                if (resultCode == RESULT_OK) {
                    String fileName;
                    String filePath;
                    try {
                        JSONArray jsonArray = new JSONArray(data.getStringExtra("data"));
                        final JSONObject object;
                        if (jsonArray.length() > 0) {
                            object = jsonArray.getJSONObject(0);
                            fileName = object.getString("file_name");
                            filePath = object.getString("file_path");

                            addChattingItem("file:" + filePath + "&" + fileName, "[文件]", MsgType.FILE_MINE);


                            int intType = MsgType.FILE_OTHERS.ordinal();
                            String inClass = targetClass.getClassID();
                            AVIMFileMessage message = null;
                            try {
                                AVFile targetFile = AVFile.withAbsoluteLocalPath(fileName,filePath);
                                targetFile.addMetaData("type",object.getInt("file_type"));
                                message = new AVIMFileMessage(targetFile);
                                Map<String,Object> attrs = new HashMap<>();
                                attrs.put("type",intType);
                                attrs.put("inClass",inClass);
                                attrs.put("fileName",fileName);
                                message.setAttrs(attrs);
//                                message.setProgressCallback(new ProgressCallback() {
//                                    @Override
//                                    public void done(Integer integer) {
//                                        Log.e("Uploading",integer + "");
//                                    }
//                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (curConversation == null) {
                                messageBuffer.add(message);
                                NormalUtils.INSTANCE.showToast(ChattingActivity.this,"网络状态不佳");
                            } else {
                                curConversation.sendMessage(message, new AVIMConversationCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        if (e != null) {
                                            NormalUtils.INSTANCE.showError(ChattingActivity.this, e);
                                        }
                                    }
                                });
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this,"没有选择任何文件~",Toast.LENGTH_SHORT).show();
                }
                break;
            case SelectFilesActivity.ACTION_SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    String imgPath;
                    String imgName;
                    try {
                        JSONArray jsonArray = new JSONArray(data.getStringExtra("data"));
                        int size = jsonArray.length();
                        for (int i = 0;i < size;i++) {

                            JSONObject object = jsonArray.getJSONObject(i);
                            imgPath = object.getString("file_path");
                            imgName = object.getString("file_name");

                            addChattingItem("picture:" + imgPath, "[图片]", MsgType.NORMAL_MINE);

                            int intType = MsgType.NORMAL_OTHERS.ordinal();
                            String inClass = targetClass.getClassID();
                            AVIMImageMessage message = null;
                            try {
                                String targetPath = BitmapUtils.INSTANCE.getLocalThumbnail(imgPath);
                                AVFile imgFile = AVFile.withAbsoluteLocalPath(imgName,targetPath);
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                BitmapFactory.decodeFile(imgPath,options);
                                imgFile.addMetaData("width", options.outWidth);
                                imgFile.addMetaData("height",options.outHeight);
                                message = new AVIMImageMessage(imgFile);
                                Map<String,Object> attrs = new HashMap<>();
                                attrs.put("type",intType);
                                attrs.put("inClass",inClass);
                                message.setAttrs(attrs);
//                                message.setProgressCallback(new ProgressCallback() {
//                                    @Override
//                                    public void done(Integer integer) {
//                                        Log.e("Uploading",integer + "");
//                                    }
//                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (curConversation == null) {
                                messageBuffer.add(message);
                                NormalUtils.INSTANCE.showToast(ChattingActivity.this,"网络状态不佳");
                            } else {
                                curConversation.sendMessage(message, new AVIMConversationCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        if (e != null) {
                                            NormalUtils.INSTANCE.showError(ChattingActivity.this, e);
                                        }
                                    }
                                });
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(this,"没有选择任何图片~",Toast.LENGTH_SHORT).show();
                }
                break;
            case ACTION_GET_EXCEL_MODEL:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra("model_name");
                    String questions = data.getStringExtra("data");

                    sendMessageToTarget("excel_task:" + name + "&" + questions, MsgType.EXCEL_OTHERS);
                    addChattingItem("normal:" + "向 " + targetUsers.get(0).getUserRealName()
                            + " 发出信息录制请求", "[发出信息录制]", MsgType.NORMAL_MINE);

                } else {
                    Log.e("TAG","无内容");
                }
                break;
            default:
                break;
        }
    }



    private class MyAdapter extends BaseAdapter {

        private List<MessageObject> items;

        private Context context;

        private static final int ITEM_TYPE_COUNT = 6;

        public MyAdapter(Context context, List<MessageObject> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public MessageObject getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            MessageObject item = getItem(position);
            return item.getType();
        }

        @Override
        public int getViewTypeCount() {
            return ITEM_TYPE_COUNT;
        }

        private class NormalMineViewHolder {
            public ImageView headerImageView;

            public TextView contentTextView;
        }

        private class NormalOthersViewHolder {
            public ImageView headerImageView;

            public TextView contentTextView;

            public TextView nameTextView;

            public ImageView contentImageView;
        }

        private class ExcelMineViewHolder {
            public ImageView headerImageView;

            public TextView contentTextView;

            public LinearLayout contentContainer;
        }

        private class ExcelOthersViewHolder {
            public ImageView headerImageView;

            public TextView contentTextView;

            public LinearLayout contentContainer;

            public TextView nameTextView;
        }

        private class FileOthersViewHolder {
            public ImageView headerImageView;

            public TextView contentTextView;

            public ImageView fileTypeImageView;

            public LinearLayout contentContainer;

            public TextView nameTextView;
        }

        private class FileMineViewHolder {
            public ImageView headerImageView;

            public TextView contentTextView;

            public ImageView fileTypeImageView;

            public LinearLayout contentContainer;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final MessageObject item = getItem(position);
            MsgType type = MsgType.valueOf(item.getType());
            NormalMineViewHolder mineViewHolder = null;
            NormalOthersViewHolder othersViewHolder = null;
            ExcelMineViewHolder excelMineViewHolder = null;
            ExcelOthersViewHolder excelOthersViewHolder = null;
            FileMineViewHolder fileMineViewHolder = null;
            FileOthersViewHolder fileOthersViewHolder = null;

            if (convertView == null) {
                switch (type) {
                    case NORMAL_MINE:
                        convertView = View.inflate(context,R.layout.item_chatting_mine_normal_msg,null);
                        mineViewHolder = new NormalMineViewHolder();
                        mineViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.tv_chatting_msg_mine_normal_content);
                        mineViewHolder.contentTextView.setMaxWidth(messageBoxMaxWidth);
                        mineViewHolder.headerImageView = (ImageView) convertView.findViewById(R.id.iv_chatting_msg_mine_normal_header);
                        mineViewHolder.headerImageView.setImageBitmap(mineHeaderImg);
                        convertView.setTag(mineViewHolder);
                        break;
                    case NORMAL_OTHERS:
                        convertView = View.inflate(context,R.layout.item_chatting_others_normal_msg,null);
                        othersViewHolder = new NormalOthersViewHolder();
                        othersViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.tv_chatting_msg_others_normal_content);
                        othersViewHolder.contentTextView.setMaxWidth(messageBoxMaxWidth);
                        othersViewHolder.headerImageView = (ImageView) convertView.findViewById(R.id.iv_chatting_msg_others_normal_header);
                        othersViewHolder.nameTextView = (TextView) convertView.findViewById(R.id.tv_chatting_msg_others_normal_name);
                        othersViewHolder.contentImageView = (ImageView) convertView.findViewById(R.id.iv_chatting_msg_others_normal_content);
                        convertView.setTag(othersViewHolder);
                        break;
                    case EXCEL_MINE:
                        convertView = View.inflate(context,R.layout.item_chatting_mine_excel,null);
                        excelMineViewHolder = new ExcelMineViewHolder();
                        excelMineViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.tv_chatting_excel_mine_content);
                        excelMineViewHolder.headerImageView = (ImageView) convertView.findViewById(R.id.iv_chatting_excel_mine_header);
                        excelMineViewHolder.contentContainer = (LinearLayout) convertView.findViewById(R.id.ll_chatting_item_mine_excel_container);
                        convertView.setTag(excelMineViewHolder);
                        break;
                    case EXCEL_OTHERS:
                        convertView = View.inflate(context,R.layout.item_chatting_others_excel,null);
                        excelOthersViewHolder = new ExcelOthersViewHolder();
                        excelOthersViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.tv_chatting_excel_others_content);
                        excelOthersViewHolder.headerImageView = (ImageView) convertView.findViewById(R.id.iv_chatting_excel_others_header);
                        excelOthersViewHolder.contentContainer = (LinearLayout) convertView.findViewById(R.id.ll_chatting_item_others_excel_container);
                        excelOthersViewHolder.nameTextView = (TextView) convertView.findViewById(R.id.tv_chatting_excel_others_name);
                        convertView.setTag(excelOthersViewHolder);
                        break;
                    case FILE_MINE:
                        convertView = View.inflate(context,R.layout.item_chatting_mine_file,null);
                        fileMineViewHolder = new FileMineViewHolder();
                        fileMineViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.tv_chatting_file_mine_content);
                        fileMineViewHolder.headerImageView = (ImageView) convertView.findViewById(R.id.iv_chatting_file_mine_header);
                        fileMineViewHolder.fileTypeImageView = (ImageView) convertView.findViewById(R.id.iv_chatting_file_mine_type);
                        fileMineViewHolder.contentContainer = (LinearLayout) convertView.findViewById(R.id.ll_chatting_item_mine_file_container);
                        convertView.setTag(fileMineViewHolder);
                        break;
                    case FILE_OTHERS:
                        convertView = View.inflate(context,R.layout.item_chatting_others_file,null);
                        fileOthersViewHolder = new FileOthersViewHolder();
                        fileOthersViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.tv_chatting_file_others_content);
                        fileOthersViewHolder.headerImageView = (ImageView) convertView.findViewById(R.id.iv_chatting_file_others_header);
                        fileOthersViewHolder.fileTypeImageView = (ImageView) convertView.findViewById(R.id.iv_chatting_file_others_type);
                        fileOthersViewHolder.contentContainer = (LinearLayout) convertView.findViewById(R.id.ll_chatting_item_others_file_container);
                        fileOthersViewHolder.nameTextView = (TextView) convertView.findViewById(R.id.tv_chatting_file_others_name);
                        convertView.setTag(fileOthersViewHolder);
                        break;
                    default:
                }
            } else {
                switch (type) {
                    case NORMAL_MINE:
                        mineViewHolder = (NormalMineViewHolder) convertView.getTag();
                        break;
                    case NORMAL_OTHERS:
                        othersViewHolder = (NormalOthersViewHolder) convertView.getTag();
                        break;
                    case EXCEL_MINE:
                        excelMineViewHolder = (ExcelMineViewHolder) convertView.getTag();
                        break;
                    case EXCEL_OTHERS:
                        excelOthersViewHolder = (ExcelOthersViewHolder) convertView.getTag();
                        break;
                    case FILE_MINE:
                        fileMineViewHolder = (FileMineViewHolder) convertView.getTag();
                        break;
                    case FILE_OTHERS:
                        fileOthersViewHolder = (FileOthersViewHolder) convertView.getTag();
                        break;
                    default:
                }
            }

            switch (type) {
                case NORMAL_MINE:
                    final String [] partedMsg = item.getContent().split(":");
                    if (partedMsg[0].equals("normal")) {
                        parseStr(partedMsg[1],mineViewHolder.contentTextView,true);
                    } else {
                        BitmapUtils.INSTANCE.loadLocalThumbnail(mineViewHolder.contentTextView, partedMsg[1]);
                    }

                    mineViewHolder.contentTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (partedMsg[0].equals("picture")) {
                                Intent intent = new Intent(ChattingActivity.this,BrowsePhotoActivity.class);
                                intent.putExtra("targetPhoto", partedMsg[1]);
                                intent.putExtra("is_local_picture",true);
                                startActivity(intent);
                            }
                        }
                    });
                    break;
                case NORMAL_OTHERS:
                    final UserObject targetObject = item.getInUserObject();
                    String imgPath = targetObject.getImgId();
                    if (imgPath.equals("")) {
                        othersViewHolder.headerImageView.setImageResource(R.drawable.default_ic_contact);
                    } else {
                        FlareBitmapUtils.INSTANCE.loadBitmap(othersViewHolder.headerImageView,imgPath);
                    }
                    othersViewHolder.nameTextView.setText(targetObject.getUserRealName());

                    final String [] partedMsg2 = item.getContent().split(":");
                    if (partedMsg2[0].equals("normal")) {
                        othersViewHolder.contentTextView.setVisibility(View.VISIBLE);
                        othersViewHolder.contentImageView.setVisibility(View.GONE);
                        parseStr(partedMsg2[1], othersViewHolder.contentTextView,false);
                    } else {
                        othersViewHolder.contentTextView.setVisibility(View.GONE);
                        othersViewHolder.contentImageView.setVisibility(View.VISIBLE);
                        FlareBitmapUtils.INSTANCE.loadBitmap(othersViewHolder.contentImageView, partedMsg2[1]);
                    }

                    othersViewHolder.contentTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });

                    othersViewHolder.contentImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (partedMsg2[0].equals("picture")) {
                                Intent intent = new Intent(ChattingActivity.this,BrowsePhotoActivity.class);
                                intent.putExtra("targetPhoto", partedMsg2[1]);
                                intent.putExtra("is_local_picture",false);
                                startActivity(intent);
                            }
                        }
                    });


                    //如果是群聊的话，点击对方的头像可以直接进入单聊模式
                    if (isGroup) {
                        othersViewHolder.headerImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ChattingActivity.this,ChattingActivity.class);
                                intent.putExtra("target",targetObject.getId());
                                intent.putExtra("is_group",false);
                                startActivity(intent);
                                ChattingActivity.this.finish();
                            }
                        });
                    }
                    break;
                case EXCEL_MINE:
                    excelMineViewHolder.headerImageView.setImageBitmap(mineHeaderImg);
                    excelMineViewHolder.contentTextView.setText(item.getContent());
                    excelMineViewHolder.contentContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                    break;
                case EXCEL_OTHERS:
                    final UserObject targetObject2 = item.getInUserObject();
                    if (targetObject2.getImgId().equals("")) {
                        excelOthersViewHolder.headerImageView.setImageResource(R.drawable.default_ic_contact);
                    } else {
                        FlareBitmapUtils.INSTANCE.loadBitmap(excelOthersViewHolder.headerImageView, targetObject2.getImgId());
                    }
                    excelOthersViewHolder.nameTextView.setText(targetObject2.getUserRealName());
                    final String [] partedExcelTask = item.getContent().split("&");
                    if (partedExcelTask.length == 2) {
                        //如果只有两块，则是接受了信息录制任务且未填写，调出填写界面
                        excelOthersViewHolder.contentContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ChattingActivity.this, SingleExcelActivity.class);
                                intent.putExtra("single_excel_title",partedExcelTask[0].split(":")[1]);
                                intent.putExtra("single_excel_question",partedExcelTask[1]);
                                intent.putExtra("single_excel_answers","");
                                intent.putExtra("single_excel_target",targetUsers.get(0).getUserId());
                                intent.putExtra("target_conversation",targetConversation);
                                startActivity(intent);
                            }
                        });
                    } else if(partedExcelTask.length == 3){
                        //如果有三块就是收到的是信息录制单人的反馈
                        excelOthersViewHolder.contentContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ChattingActivity.this, SingleExcelActivity.class);
                                intent.putExtra("single_excel_title",partedExcelTask[0].split(":")[1]);
                                intent.putExtra("single_excel_question",partedExcelTask[1]);
                                intent.putExtra("single_excel_answers",partedExcelTask[2]);
                                intent.putExtra("single_excel_target","");
                                startActivity(intent);
                            }
                        });
                    }
                    excelOthersViewHolder.contentTextView.setText(partedExcelTask[0].split(":")[1]);

                    //如果是群聊的话，点击对方的头像可以直接进入单聊模式
                    if (isGroup) {
                        excelOthersViewHolder.headerImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ChattingActivity.this,ChattingActivity.class);
                                intent.putExtra("target",targetObject2.getId());
                                intent.putExtra("is_group",false);
                                startActivity(intent);
                                ChattingActivity.this.finish();
                            }
                        });
                    }
                    break;
                case FILE_MINE:
                    fileMineViewHolder.headerImageView.setImageBitmap(mineHeaderImg);
                    final String [] fileParted = item.getContent().split("&");
                    String name;
                    if (fileParted.length == 2) {
                        name = fileParted[1];
                    } else {
                        name = item.getContent();
                    }
                    fileMineViewHolder.contentTextView.setText(name);

                    fileMineViewHolder.contentContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            parseFileItem(item.getContent());
                            FileUtils.getInstance().openFile(new File(fileParted[0].split(":")[1]),ChattingActivity.this);
                        }
                    });

                    Bitmap bitmap = getBitmapFromName(item.getContent());
                    fileMineViewHolder.fileTypeImageView.setImageBitmap(bitmap);
                    break;
                case FILE_OTHERS:
                    final UserObject targetObject4 = item.getInUserObject();
                    if (targetObject4.getImgId().equals("")) {
                        fileOthersViewHolder.headerImageView.setImageResource(R.drawable.default_ic_contact);
                    } else {
                        FlareBitmapUtils.INSTANCE.loadBitmap(fileOthersViewHolder.headerImageView, targetObject4.getImgId());
                    }

                    fileOthersViewHolder.nameTextView.setText(targetObject4.getUserRealName());
                    String [] fileParted2 = item.getContent().split("&");
                    String name2;
                    if (fileParted2.length == 2) {
                        name2 = fileParted2[1];
                    } else {
                        name2 = item.getContent();
                    }
                    fileOthersViewHolder.contentTextView.setText(name2);

                    fileOthersViewHolder.contentContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            parseFileItem(item.getContent());
                        }
                    });

                    Bitmap bitmap2 = getBitmapFromName(item.getContent());
                    fileOthersViewHolder.fileTypeImageView.setImageBitmap(bitmap2);

                    //如果是群聊的话，点击对方的头像可以直接进入单聊模式
                    if (isGroup) {
                        fileOthersViewHolder.headerImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ChattingActivity.this,ChattingActivity.class);
                                intent.putExtra("target",targetObject4.getId());
                                intent.putExtra("is_group",false);
                                startActivity(intent);
                                ChattingActivity.this.finish();
                            }
                        });
                    }
                    break;
                default:
            }
            return convertView;
        }

        private BitmapFactory.Options options = new BitmapFactory.Options();

        private void  parseStr(String resource,TextView target,boolean isMine)
        {
            Pattern pattern  = Pattern.compile("\\[emoji\\d{3}");
            Matcher matcher = pattern.matcher(resource);

            String strFace;
            int index = 0;
            int start;
            int end = 0;

            if (isMine) {
                target.setPadding(6 * padding2dp, 5 * padding2dp, 8 * padding2dp, 7 * padding2dp);
            }
            //如果没有表情的输入，直接返回填充字符即可
            boolean hasEmoji = false;
            if (!matcher.find()) {
                target.setText(resource);
            } else {
                hasEmoji = true;
                target.setText("");
            }

            if (hasEmoji) {
                matcher.reset();
                while (matcher.find()) {
                    strFace = matcher.group();
                    start = matcher.start();
                    end = matcher.end();
                    try {
                        Field field = R.mipmap.class.getDeclaredField(matcher.group().substring(1));
                        int resourceId = 0;
                        try {
                            resourceId = Integer.parseInt(field.get(null).toString());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
                        ImageSpan imageSpan = new ImageSpan(ChattingActivity.this, bitmap);
                        SpannableString spannableString = new SpannableString(strFace);
                        spannableString.setSpan(imageSpan, 0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        target.append(resource.substring(index, start));
                        target.append(spannableString);
                        index = end;
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
                if (end != resource.length() && end != 0) {
                    target.append(resource.substring(end));
                }
            }
        }

        private void parseFileItem(String content) {
            String [] fileParted = content.split("&");
            String objectId = "";
            if (fileParted.length == 2) {
                objectId = fileParted[0].split(":")[1];
            }
            if (!objectId.equals("")) {
                AVFile.withObjectIdInBackground(objectId, new GetFileCallback<AVFile>() {
                    @Override
                    public void done(final AVFile avFile, AVException e) {
                        if (e == null) {
                            //获取文件基本信息
                            final String fileName = avFile.getOriginalName();
                            final long fileSize = avFile.getSize();
                            final int type = (Integer) avFile.getMetaData("type");

                            List<LocalFileObject> localFileObjects = DataSupport.where("fileName=? and size=?",
                                    fileName, String.valueOf(fileSize)).
                                    find(LocalFileObject.class);
                            if (localFileObjects.size() != 0) {
                                //首先判断本机是否存在指定文件
                                File targetFile = new File(localFileObjects.get(0).getFilePath());
                                if (targetFile.exists()) {
                                    //如果已经存在，直接打开
                                    FileUtils.getInstance().openFile(targetFile, ChattingActivity.this);
                                    return;
                                } else {
                                    //为了避免出现重复的数据，先将就数据删除
                                    localFileObjects.get(0).delete();
                                }
                            }
                            //如果文件不在本地或者路径失效,开始从网络上下载
                            String infoFormat = "文件名称 : %s \n 文件大小 : %s";
                            final Dialog dialog = new Dialog(ChattingActivity.this,"文件尚未下载",
                                    String.format(infoFormat, fileName, NormalUtils.INSTANCE.sizeToString(fileSize)));
                            dialog.addCancelButton("取消");
                            dialog.setButtonAcceptText("确认下载");
                            dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final DownloadProgressDialog downloadProgressDialog = DownloadProgressDialog.getDownloadDialog();
                                    downloadProgressDialog.show(getSupportFragmentManager(),"download_dialog");
                                    avFile.getDataInBackground(new GetDataCallback() {
                                        @Override
                                        public void done(byte[] bytes, AVException e) {
                                            if (e == null) {
                                                LocalFileObject newFile = new LocalFileObject();
                                                newFile.setTime(new Date().getTime());
                                                newFile.setSize(fileSize);
                                                newFile.setType(type);
                                                String source = (targetUsers.size() > 1) ?
                                                        targetSubClass.getSubClassName() : targetUsers.get(0).getUserRealName();
                                                newFile.setSource("下载自 " + source);
                                                newFile.setFileName(fileName);

                                                String path = FileUtils.getInstance().saveNormalFile(bytes,fileName);
                                                newFile.setFilePath(path);
                                                newFile.save();

                                                //判断是否更新FileObject班级文件对象
                                                List<FileObject> temp = DataSupport.where("name = ? and size = ?",
                                                        fileName,String.valueOf(fileSize)).find(FileObject.class);
                                                if (temp.size() > 0) {
                                                    FileObject updateClassFile = temp.get(0);
                                                    updateClassFile.setPath(path);
//                                                    updateClassFile.setUpdateTime(new Date());
                                                    updateClassFile.update(updateClassFile.getId());
                                                }
                                                FileUtils.getInstance().openFile(new File(newFile.getFilePath()), CMApplication.GLOBAL_CONTEXT);
                                            } else {
                                                NormalUtils.INSTANCE.showError(ChattingActivity.this, e);
                                            }
                                        }
                                    }, new ProgressCallback() {
                                        @Override
                                        public void done(Integer integer) {
                                            downloadProgressDialog.setProgress(integer);
                                        }
                                    });
                                }
                            });
                            dialog.show();

//                            NormalUtils.INSTANCE.showToast(ChattingActivity.this, "开始下载文件~");
//                            avFile.getDataInBackground(new GetDataCallback() {
//                                @Override
//                                public void done(byte[] bytes, AVException e) {
//                                    if (e == null) {
//                                        LocalFileObject newFile = new LocalFileObject();
//                                        newFile.setTime(new Date().getTime());
//                                        newFile.setSize(avFile.getSize());
//                                        newFile.setType((Integer) avFile.getMetaData("type"));
//                                        String source = (targetUsers.size() > 1) ?
//                                                targetSubClass.getSubClassName() : targetUsers.get(0).getUserRealName();
//                                        newFile.setSource("下载自 " + source);
//                                        newFile.setFileName(avFile.getOriginalName());
//
//                                        String path = FileUtils.getInstance().saveNormalFile(bytes,avFile.getOriginalName());
//                                        newFile.setFilePath(path);
//                                        newFile.save();
//
//                                        //判断是否更新FileObject班级文件对象
//                                        List<FileObject> temp = DataSupport.where("name = ? and size = ?",
//                                                avFile.getOriginalName(),String.valueOf(avFile.getSize())).find(FileObject.class);
//                                        if (temp.size() > 0) {
//                                            FileObject updateClassFile = temp.get(0);
//                                            updateClassFile.setPath(path);
//                                            updateClassFile.setUpdateTime(new Date());
//                                            updateClassFile.update(updateClassFile.getId());
//                                        }
//                                        FileUtils.getInstance().openFile(new File(newFile.getFilePath()), CMApplication.GLOBAL_CONTEXT);
//
//                                    } else {
//                                        NormalUtils.INSTANCE.showError(ChattingActivity.this,e);
//                                    }
//                                }
//                            });
                        } else {
                            NormalUtils.INSTANCE.showError(ChattingActivity.this,e);
                        }
                    }
                });

            }
        }

        private Bitmap getBitmapFromName(String bitmapName) {
            FileType type = FileUtils.getInstance().getFileType(bitmapName);
            Bitmap bitmap;
            switch (type) {
                case DOCUMENT:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_doc);
                    break;
                case PICTURE:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_ic_pic);
                    break;
                case COMPRESSED:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_zip);
                    break;
                case VIDEO:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_video);
                    break;
                case VOICE:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_pic_voice);
                    break;
                case APPLICATION:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_pic_app);
                    break;
                case SOURCE_FILE:
                default:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_others);
                    break;
            }
            return bitmap;
        }
    }
}
