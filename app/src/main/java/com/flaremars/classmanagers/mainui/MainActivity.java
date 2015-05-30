package com.flaremars.classmanagers.mainui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.PushService;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.AnnouncementObject;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.BaseGlobalData;
import com.flaremars.classmanagers.model.ClassMemberObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.DataWrapper;
import com.flaremars.classmanagers.model.ExcelFeedbackObject;
import com.flaremars.classmanagers.model.ExcelObject;
import com.flaremars.classmanagers.model.ExcelTaskObject;
import com.flaremars.classmanagers.model.FragmentConst;
import com.flaremars.classmanagers.model.ManagerObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.NoticeGroupMemberObject;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.NoticeObject;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.service.CMService;
import com.flaremars.classmanagers.uis.AnnouncementsFragment;
import com.flaremars.classmanagers.uis.ContainerActivity;
import com.flaremars.classmanagers.views.ContextMenuDialog;
import com.flaremars.classmanagers.uis.JoinClassActivity;
import com.flaremars.classmanagers.uis.SearchActivity;
import com.flaremars.classmanagers.utils.FlareBitmapUtils;
import com.flaremars.classmanagers.utils.MessagePostUtil;
import com.flaremars.classmanagers.utils.MessageUtils;
import com.flaremars.classmanagers.utils.NotificationUtils;
import com.flaremars.classmanagers.views.ListViewCompat;
import com.flaremars.classmanagers.views.RoundImageView;
import com.flaremars.classmanagers.views.SlideView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements SlideView.OnSlideListener{

    public static final int ACTION_GET_MODEL = 1;

    public static final int ACTION_GET_TARGETS = 2;

    private static final Date TODAY = new Date();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

    private static final SimpleDateFormat DATE_FORMAT_FULL = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);

    public static final BaseGlobalData BASE_GLOBAL_DATA = new BaseGlobalData();

    private FrameLayout mainContainer;

    /*最近被滑动的子项view*/
    private static SlideView mLastSlideViewWithStatusOn;

    private int drawerWidth;

    private int flexibleSpaceImageHeight;

    private int smallHeaderHeight;

    private LinearLayout headerView;

    private LinearLayout smallHeaderView;

    private ViewPager announcementsViewPager;

    private TextView fastSendAnnouncement;

    private TextView announcementsSizeIndicator;

    private View coverPage;

    private FloatingActionsMenu functionFloatingMenu;

    private List<AnnouncementsFragment> announcementsFragments;

    private List<AnnouncementObject> announcements;

    private TestPagerAdapter pagerAdapter;

    private AnnouncementHandler handler = new AnnouncementHandler(new WeakReference<>(this));

    private DrawerLayout drawerLayout;

    //关键数据
    private ClassObject curClass;

    private UserObject mineObject;

    //标识是否有新的公告数据
    public static boolean hasNewAnnouncement = false;

    //聊天相关
    private List<RecentMessageObject> recentMessages;

    private ChattingAdapter chattingAdapter;

    private RoundImageView roundImageView;

    //当前唯一Activity 的实例
    public static MainActivity MainActivityInstance;

    private ExcelDataWrapper tempExcelDataWrapper;

    private class ExcelDataWrapper {
        private String modelName;

        private String questions;

        public ExcelDataWrapper(String modelName, String questions) {
            this.modelName = modelName;
            this.questions = questions;
        }

        public String getModelName() {
            return modelName;
        }

        public String getQuestions() {
            return questions;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_material_menu);
        MainActivityInstance = this;

        PushService.setDefaultPushCallback(this, MainActivity.class);

        /*初始化全局内容*/
        SharedPreferences preferences = getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,MODE_PRIVATE);
        BASE_GLOBAL_DATA.setCurClassID(preferences.getString(AppConst.CUR_CLASS_ID, ""));
        BASE_GLOBAL_DATA.setCurClassName(preferences.getString(AppConst.CUR_CLASS_NAME, ""));
        BASE_GLOBAL_DATA.setUserHeaderId(preferences.getString(AppConst.USER_HEADER_ID, ""));
        //这里前面有登陆界面，所以可以保证有值
        BASE_GLOBAL_DATA.setUserName(preferences.getString(AppConst.USER_NAME, ""));
        BASE_GLOBAL_DATA.setUserID(preferences.getString(AppConst.USER_ID, ""));
        BASE_GLOBAL_DATA.setUserRealName(preferences.getString(AppConst.USER_REAL_NAME, ""));


        Log.e("TAG", BASE_GLOBAL_DATA.getUserID());
        /*获取数据*/
        if (BASE_GLOBAL_DATA.getCurClassID().equals("")) {
            curClass = null;
        } else {
            curClass = DataSupport.where("classID=?",BASE_GLOBAL_DATA.getCurClassID()).find(ClassObject.class).get(0);
            startCMService();
        }
        mineObject = DataSupport.where("userId=?",BASE_GLOBAL_DATA.getUserID()).find(UserObject.class).get(0);

        /*必要UI数值*/
        flexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.main_header_height);
        smallHeaderHeight = getResources().getDimensionPixelSize(R.dimen.main_small_header_height);
        drawerWidth = getResources().getDimensionPixelSize(R.dimen.drawer_width);

        /*获取各项控件实例*/
        roundImageView = (RoundImageView) findViewById(R.id.drawer_img);
        TextView userNameTextView = (TextView) findViewById(R.id.tv_drawer_name);
        ListViewCompat chattingListView = (ListViewCompat) findViewById(R.id.list_view);
        drawerLayout = ((DrawerLayout) findViewById(R.id.drawer_layout));
        ImageView drawerToggle = (ImageView) findViewById(R.id.drawer_toggle);

        headerView = (LinearLayout) findViewById(R.id.main_ll_header);
        smallHeaderView = (LinearLayout) findViewById(R.id.main_ll_small_header);
        announcementsViewPager = (ViewPager) findViewById(R.id.main_vp_announcements);
        announcementsSizeIndicator = (TextView) findViewById(R.id.main_tv_size_indicator);
        fastSendAnnouncement = (TextView) findViewById(R.id.tv_btn_send_announcement_when_none);
        mainContainer = (FrameLayout) findViewById(R.id.main_content);
        functionFloatingMenu = (FloatingActionsMenu) findViewById(R.id.main_function_fb);
        coverPage = findViewById(R.id.main_white_cover_floor);

        //初始化显示界面
        roundImageView.setBorderOutsideColor(Color.TRANSPARENT);
        roundImageView.setBorderInsideColor(Color.TRANSPARENT);
        roundImageView.setImageResource(R.drawable.default_ic_contact);
        userNameTextView.setText(BASE_GLOBAL_DATA.getUserRealName());
        coverPage.setVisibility(View.GONE);

        //初始化FloatingMenu
        functionFloatingMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                coverPage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                coverPage.setVisibility(View.GONE);
            }
        });

        coverPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                functionFloatingMenu.collapse();
            }
        });

        FloatingActionButton sendAnnouncementButton = new FloatingActionButton(this);
        sendAnnouncementButton.setSize(FloatingActionButton.SIZE_MINI);
        sendAnnouncementButton.setTitle("公告");
        sendAnnouncementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapseMenuByDelay();
                Intent intent = new Intent(MainActivity.this, ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_CREATE_ANNOUNCEMENT);
                bundle.putString(AppConst.CLASS_ID, MainActivity.BASE_GLOBAL_DATA.getCurClassID());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        FloatingActionButton sendExcelTaskButton = new FloatingActionButton(this);
        sendExcelTaskButton.setSize(FloatingActionButton.SIZE_MINI);
        sendExcelTaskButton.setTitle("信息录制");
        sendExcelTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapseMenuByDelay();
                Intent intent = new Intent(MainActivity.this, ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_EXCEL_MODELS);
                bundle.putBoolean(AppConst.IS_POINT_TO_PERSON, true);
                intent.putExtras(bundle);
                startActivityForResult(intent, ACTION_GET_MODEL);
            }
        });
        FloatingActionButton sendNoticeButton = new FloatingActionButton(this);
        sendNoticeButton.setSize(FloatingActionButton.SIZE_MINI);
        sendNoticeButton.setTitle("通知");
        sendNoticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapseMenuByDelay();
                List<NoticeGroupObject> noticeGroups = DataSupport.where("classobject_id=?", String.valueOf(curClass.getId())).find(NoticeGroupObject.class);
                int size = noticeGroups.size();
                if (size > 1) {
                    Intent intent3 = new Intent(MainActivity.this, ContainerActivity.class);
                    intent3.putExtra(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_NOTICE_GROUPS);
                    intent3.putExtra("is_show_input_layout", true);
                    startActivity(intent3);
                } else {
                    Intent intent = new Intent(MainActivity.this, NoticeGroupActivity.class);
                    List<NoticeGroupObject> firstGroup = DataSupport.where("classobject_id=? and isAllMember = 1", String.valueOf(curClass.getId())).find(NoticeGroupObject.class);
                    if (firstGroup.size() > 0) {
                        intent.putExtra("target_notice_group", firstGroup.get(0).getId());
                    } else {
                        NoticeGroupObject allMemberNoticeGroup = createAllMemberNoticeGroup();
                        intent.putExtra("target_notice_group", allMemberNoticeGroup.getId());
                    }
                    intent.putExtra("is_show_input_layout", true);
                    startActivity(intent);
                }
            }
        });

        sendAnnouncementButton.setColorNormalResId(R.color.main_color);
        sendAnnouncementButton.setIcon(R.drawable.note);
        sendExcelTaskButton.setColorNormalResId(R.color.main_color);
        sendExcelTaskButton.setIcon(R.drawable.luzhi);
        sendNoticeButton.setColorNormalResId(R.color.main_color);
        sendNoticeButton.setIcon(R.drawable.tongzhi);
        functionFloatingMenu.addButton(sendAnnouncementButton);
        functionFloatingMenu.addButton(sendExcelTaskButton);
        functionFloatingMenu.addButton(sendNoticeButton);

        /*Drawer相关设置*/
        drawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });
        drawerLayout.setScrimColor(Color.parseColor("#66000000"));
        drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                int offset = (int) (slideOffset * drawerWidth) - 40;
                offset = Math.max(0, offset);
                ViewHelper.setTranslationX(mainContainer, offset);
                if (MainActivity.getSlideView() != null) {
                    MainActivity.getSlideView().fastShrink();
                }
            }

            //本来处于打开状态
            @Override
            public void onDrawerOpened(android.view.View drawerView) {
            }

            //本来处于关闭状态
            @Override
            public void onDrawerClosed(android.view.View drawerView) {

            }
        });

        fastSendAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_CREATE_ANNOUNCEMENT);
                bundle.putString(AppConst.CLASS_ID, MainActivity.BASE_GLOBAL_DATA.getCurClassID());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        //头像的点击响应
        roundImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawerByDelay();
                Intent i = new Intent(MainActivity.this, ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_PERSONAL_CENTER);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        //搜索图标被点击
        findViewById(R.id.main_screen_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        //班级简介位置点击响应
        findViewById(R.id.header_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curClass != null) {
                    Intent i = new Intent(MainActivity.this, ContainerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_CLASS_CENTER);
                    bundle.putString(AppConst.CLASS_ID, curClass.getClassID());
                    i.putExtras(bundle);
                    startActivity(i);
                } else {
                    Intent intent = new Intent(MainActivity.this, JoinClassActivity.class);
                    startActivity(intent);
                }
            }
        });

        //关联消息点击响应
        findViewById(R.id.drawer_view_related_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawerByDelay();
                Intent i = new Intent(MainActivity.this, ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_OTHER_CLASS_MESSAGE);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        //收藏点击响应

        //已下载点击响应
        findViewById(R.id.drawer_view_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawerByDelay();
                Intent i = new Intent(MainActivity.this, ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_DOWNLOADED_FILES);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        //添加点击响应
        findViewById(R.id.drawer_view_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawerByDelay();
                Intent i = new Intent(MainActivity.this, JoinClassActivity.class);
                startActivity(i);
            }
        });

        //设置点击相应
        findViewById(R.id.drawer_view_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawerByDelay();
                Intent intent = new Intent(MainActivity.this, ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_SETTING);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        /*三大入口处理*/
        findViewById(R.id.function_bar_files).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_FILES);
                bundle.putString(AppConst.CLASS_ID, curClass.getClassID());
                bundle.putBoolean(AppConst.NEED_ADD_TO_BACK_STACK, false);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        findViewById(R.id.function_bar_contacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_CONTACTS);
                bundle.putString(AppConst.CLASS_ID, curClass.getClassID());
                bundle.putBoolean(AppConst.NEED_ADD_TO_BACK_STACK, false);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        findViewById(R.id.function_bar_albums).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_ALBUMS);
                bundle.putString(AppConst.CLASS_ID, curClass.getClassID());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        /*消息列表处理*/
        View paddingView = new View(this);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                flexibleSpaceImageHeight);
        paddingView.setLayoutParams(lp);
        paddingView.setClickable(true);
        paddingView.setBackgroundColor(Color.TRANSPARENT);
        chattingListView.addHeaderView(paddingView);

        if (curClass != null) {
            recentMessages = DataSupport.where("classobject_id=? and userobject_id=?",
                    String.valueOf(curClass.getId()),String.valueOf(mineObject.getId())).find(RecentMessageObject.class);
        }

        chattingAdapter = new ChattingAdapter(this,recentMessages);
        chattingListView.setAdapter(chattingAdapter);
        chattingListView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int i, boolean b, boolean b2) {
                updateFlexibleSpaceText(i);
            }

            @Override
            public void onDownMotionEvent() {

            }

            @Override
            public void onUpOrCancelMotionEvent(ScrollState scrollState) {

            }
        });


        chattingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position -= 1; //因为加了一个Clickable header，所有就多出一个Item

                RecentMessageObject item = recentMessages.get(position);
                int type = item.getType();
                if (type != RecentMessageObject.TYPE_EXCEL) {
                    ContentValues cv = new ContentValues();
                    cv.put("noReadNumber", 0);
                    DataSupport.update(RecentMessageObject.class, cv, item.getId());
                }
                switch (type) {
                    case RecentMessageObject.TYPE_NOTICE_GROUP:
                        Intent intent3 = new Intent(MainActivity.this, NoticeGroupActivity.class);
                        intent3.putExtra("target_notice_group", item.getTargetID());
                        intent3.putExtra("is_show_input_layout",false);
                        startActivity(intent3);
                        break;
                    case RecentMessageObject.TYPE_PERSON:
                        Intent intent = new Intent(MainActivity.this, ChattingActivity.class);
                        intent.putExtra("target", item.getTargetID());
                        intent.putExtra("is_group", false);
                        startActivity(intent);
                        break;
                    case RecentMessageObject.TYPE_GROUP:
                        Intent intent2 = new Intent(MainActivity.this, ChattingActivity.class);
                        intent2.putExtra("target", item.getTargetID());
                        SubClassObject target = DataSupport.find(SubClassObject.class,item.getTargetID());
                        intent2.putExtra("target_conversation",target.getSubClassID());
                        intent2.putExtra("is_group", true);
                        startActivity(intent2);
                        break;
                    case RecentMessageObject.TYPE_EXCEL:
                        int targetId = item.getTargetID();
                        Intent intent1 = new Intent(MainActivity.this, ContainerActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_FILL_EXCEL);
                        bundle.putInt(AppConst.TARGET_EXCEL, targetId);
                        intent1.putExtras(bundle);
                        startActivity(intent1);

                        break;
                    case RecentMessageObject.TYPE_NOTICE:
                        NoticeObject noticeObject = DataSupport.find(NoticeObject.class, item.getTargetID(), true);
                        noticeObject.setRead(true);
                        noticeObject.update(noticeObject.getId());

                        Intent openDetailIntent = new Intent(MainActivity.this, ContainerActivity.class);
                        Bundle bundle1 = new Bundle();
                        bundle1.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_NOTICE_DETAIL);
                        bundle1.putString("nameOfSender", noticeObject.getNameOfSender());
                        bundle1.putString("time", String.valueOf(noticeObject.getTime().getTime()));
                        bundle1.putString("content", noticeObject.getContent());
                        openDetailIntent.putExtras(bundle1);
                        startActivity(openDetailIntent);

                        if (item.getNoReadNumber() == 0) {
                            return;
                        } else {
                            List<String> targetIds = new ArrayList<>();
                            targetIds.add(noticeObject.getIdOfSender());
                            final JSONObject msgContent = new JSONObject();
                            try {
                                msgContent.put(MessageConst.MSG_TYPE,MessageConst.NEW_NOTICE_FEEDBACK);
                                msgContent.put(MessageConst.CONTENT_FROM_ID,MainActivity.BASE_GLOBAL_DATA.getUserID());
                                msgContent.put(MessageConst.CONTENT_TARGET_ID,noticeObject.getNoticeId());
                                msgContent.put(MessageConst.CONTENT_IN_CLASS,curClass.getClassID());
                                msgContent.put(MessageConst.CONTENT_TIME, new Date().getTime());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            DataWrapper wrapper = new DataWrapper(
                                    MainActivity.BASE_GLOBAL_DATA.getUserID(),
                                    curClass.getClassID(),
                                    "",
                                    targetIds,
                                    msgContent
                            );

                            int messageId = MessagePostUtil.INSTANCE.wannaPostMessage(wrapper);
                            Log.e("TAG", "messageId = " + messageId);
                        }

                        recentMessages.clear();
                        recentMessages.addAll(DataSupport.where("classobject_id=? and userobject_id=?",
                                String.valueOf(curClass.getId()),String.valueOf(mineObject.getId())).
                                find(com.flaremars.classmanagers.model.RecentMessageObject.class));
                        Collections.sort(recentMessages);
                        chattingAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        });
        chattingListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final RecentMessageObject item = recentMessages.get(position - 1);
                ContextMenuDialog.getFunctionDialog(new ContextMenuDialog.OnDeleteClickListener() {
                    @Override
                    public void onDeleteClick() {

                        recentMessages.remove(item);
                        DataSupport.delete(RecentMessageObject.class, item.getId());
                        chattingAdapter.notifyDataSetChanged();
                    }
                }, new ContextMenuDialog.OnUpClickListener() {
                    @Override
                    public void onUpClick() {
                        RecentMessageObject item = recentMessages.get(position - 1);
                        ContentValues cv = new ContentValues();
                        cv.put("isAlwaysTop", !item.isAlwaysTop());
                        item.setAlwaysTop(!item.isAlwaysTop());

                        DataSupport.update(RecentMessageObject.class, cv, item.getId());
                        Collections.sort(recentMessages);
                        chattingAdapter.notifyDataSetChanged();
                    }
                }, item.isAlwaysTop()).show(getSupportFragmentManager(), "context_menu");
                return true;
            }
        });
        chattingListView.setDivider(null);


        /*公告栏处理*/
        announcementsFragments = new ArrayList<>();
        announcements = new ArrayList<>();
        pagerAdapter = new TestPagerAdapter(getSupportFragmentManager(), announcementsFragments);
        announcementsViewPager.setAdapter(pagerAdapter);
        announcementsViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                handler.sendMessage(Message.obtain(handler, AnnouncementHandler.MSG_PAGE_CHANGED, position, 0));
                if (announcementsFragments.size() <= 0) {
                    return;
                }
                if (announcements.size() == 1) {
                    announcementsSizeIndicator.setText("");
                } else {
                    announcementsSizeIndicator.setText((position % announcementsFragments.size() + 1) + "/" + announcementsFragments.size());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        handler.sendEmptyMessage(AnnouncementHandler.MSG_KEEP_SILENT);
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:
                        if (announcements.size() > 1) {
                            handler.sendEmptyMessageDelayed(AnnouncementHandler.MSG_UPDATE_IMAGE,AnnouncementHandler.MSG_DELAY);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        announcementsViewPager.setCurrentItem(Integer.MAX_VALUE / 2);

    }



    private void closeDrawerByDelay() {
        drawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
//                drawerLayout.closeDrawer(Gravity.START);
            }
        },500);
    }


    private NoticeGroupObject createAllMemberNoticeGroup () {
        NoticeGroupObject targetNoticeGroup = new NoticeGroupObject();
        targetNoticeGroup.setInClass(curClass);
        targetNoticeGroup.setNoticeGroupID(DATE_FORMAT_FULL.format(new Date()));
        targetNoticeGroup.setName("[" + curClass.getName() + "] 全体成员通知组");
        targetNoticeGroup.setAllMember(true);
        targetNoticeGroup.save();

        List<ClassMemberObject> memberObjects = curClass.getMemberList();
        for (ClassMemberObject member : memberObjects) {
            NoticeGroupMemberObject newMember = new NoticeGroupMemberObject();
            newMember.setMemberName(member.getMemberName());
            newMember.setMemberID(member.getMemberID());
            newMember.setInNoticeGroup(targetNoticeGroup);
            newMember.save();
        }
        return targetNoticeGroup;
    }

    private void collapseMenuByDelay() {
        functionFloatingMenu.postDelayed(new Runnable() {
            @Override
            public void run() {
                functionFloatingMenu.collapse();
            }
        },1000);
    }

    //专为消息录制任务服务
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ACTION_GET_MODEL) {
                String name = data.getStringExtra("model_name");
                String questions = data.getStringExtra("data");
                tempExcelDataWrapper = new ExcelDataWrapper(name, questions);

                Intent intent = new Intent(this, SelectContactsActivity.class);
                intent.putExtra("is_contain_notice_group",true);
                startActivityForResult(intent, ACTION_GET_TARGETS);
            } else if (requestCode == ACTION_GET_TARGETS){
                int targetNoticeGroup;

                //TODO 跟CreateInfoCollectionFragment重构
                String contactsStr = data.getStringExtra("contacts_data");
                boolean isNewNoticeGroup = data.getBooleanExtra("is_new_notice_group",false);
                if (isNewNoticeGroup) {
                    String[] contacts = contactsStr.split("_");

                    List<UserObject> members = new ArrayList<>();
                    final StringBuilder nameStringBuilder = new StringBuilder();
                    for (String contactID : contacts) {
                        UserObject object = DataSupport.where("userID=?",contactID).find(UserObject.class).get(0);
                        members.add(object);
                        nameStringBuilder.append(object.getUserRealName());
                        nameStringBuilder.append("、");
                    }
                    nameStringBuilder.deleteCharAt(nameStringBuilder.length() - 1);

                    NoticeGroupObject noticeGroupObject = new NoticeGroupObject();
                    noticeGroupObject.setName(nameStringBuilder.toString());
                    noticeGroupObject.setNoticeGroupID(new Date().getTime() + "");
                    noticeGroupObject.setInClass(curClass);
                    noticeGroupObject.save();

                    for (UserObject object : members) {
                        NoticeGroupMemberObject memberObject = new NoticeGroupMemberObject();
                        memberObject.setInNoticeGroup(noticeGroupObject);
                        memberObject.setMemberName(object.getUserRealName());
                        memberObject.setMemberID(object.getUserId());
                        memberObject.save();
                    }

                    targetNoticeGroup = noticeGroupObject.getId();
                } else {
                    targetNoticeGroup = data.getIntExtra("target_notice_group",1);
                }

                //发出信息录制任务n
                NoticeGroupObject noticeGroup = DataSupport.find(NoticeGroupObject.class,targetNoticeGroup);
                List<NoticeGroupMemberObject> targets = noticeGroup.getMemberList();
                final List<String> targetIds = new ArrayList<>();

                ExcelTaskObject excelTaskObject = new ExcelTaskObject();
                excelTaskObject.setInNoticeGroup(noticeGroup);
                Date now = new Date();
                excelTaskObject.setExcelId(DATE_FORMAT_FULL.format(now));
                excelTaskObject.setTime(now);
                excelTaskObject.setName(tempExcelDataWrapper.modelName);
                excelTaskObject.setNewFeedback(false);
                excelTaskObject.setQuestions(tempExcelDataWrapper.questions);
                excelTaskObject.save();

                //生成初始化反馈
                for (NoticeGroupMemberObject temp : targets) {
                    targetIds.add(temp.getMemberID());

                    ExcelFeedbackObject feedbackObject = new ExcelFeedbackObject();
                    UserObject userObject = DataSupport.where("userID=?", temp.getMemberID()).find(UserObject.class).get(0);
                    feedbackObject.setAnswer("");
                    feedbackObject.setInExcelTaskObject(excelTaskObject);
                    feedbackObject.setTargetPhone(userObject.getUserName());
                    feedbackObject.setWhose(temp.getMemberID());
                    feedbackObject.setWhoseName(temp.getMemberName());
                    feedbackObject.save();
                }

                final JSONObject msgContent = new JSONObject();
                try {
                    msgContent.put(MessageConst.MSG_TYPE, MessageConst.NEW_EXCEL_COMING);
                    msgContent.put(MessageConst.CONTENT_FROM, MainActivity.BASE_GLOBAL_DATA.getUserRealName());
                    msgContent.put(MessageConst.CONTENT_FROM_ID, MainActivity.BASE_GLOBAL_DATA.getUserID());
                    msgContent.put(MessageConst.CONTENT_TARGET_ID, excelTaskObject.getExcelId());
                    msgContent.put(MessageConst.CONTENT_EXCEL_CONTENT, excelTaskObject.getQuestions());
                    msgContent.put(MessageConst.CONTENT_EXCEL_NAME, excelTaskObject.getName());
                    msgContent.put(MessageConst.CONTENT_IN_CLASS, curClass.getClassID());
                    msgContent.put(MessageConst.CONTENT_TIME, excelTaskObject.getTime().getTime());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                DataWrapper wrapper = new DataWrapper(
                        MainActivity.BASE_GLOBAL_DATA.getUserID(),
                        curClass.getClassID(),
                        "",
                        targetIds,
                        msgContent
                );

                int messageId = MessagePostUtil.INSTANCE.wannaPostMessage(wrapper);
                Log.e("TAG", "messageId = " + messageId);

                MessageUtils.INSTANCE.updateRecentMessage(curClass, excelTaskObject.getName(), noticeGroup,mineObject.getUserId());
            }
        } else {
            Log.e("TAG", "无内容");
        }
    }

    //退出MainActivity逻辑操作
    private long exitTime;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout.isDrawerOpen(Gravity.START)) {
                drawerLayout.closeDrawer(Gravity.START);
                return true;
            }
            if (functionFloatingMenu.isExpanded()) {
                functionFloatingMenu.collapse();
                return true;
            }
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void reloadUI() {
        /*获取各项控件实例*/
        final ImageView mineClassHeaderImageView = (ImageView) findViewById(R.id.iv_main_min_class_header);
        RoundImageView mineClassHeaderImageViewBig = (RoundImageView) findViewById(R.id.iv_main_big_class_header);
        TextView curClassNameTextViewBig = (TextView) findViewById(R.id.tv_main_big_class_name);
        TextView curClassNameTextView = (TextView) findViewById(R.id.tv_main_min_class_name);
        TextView curClassPeopleCountTextView = (TextView) findViewById(R.id.tv_main_class_people_count);
        TextView curClassBaseLocate = (TextView) findViewById(R.id.tv_main_class_base_locate);

        if (BASE_GLOBAL_DATA.getUserHeaderId().equals("")) {
            roundImageView.setImageResource(R.drawable.default_ic_contact);
        } else {
            roundImageView.setImageResource(R.drawable.default_ic_contact);
            FlareBitmapUtils.INSTANCE.loadBitmap(roundImageView,BASE_GLOBAL_DATA.getUserHeaderId());
        }

        if (curClass == null) {
            mineClassHeaderImageView.setImageResource(R.drawable.default_ic_mine_class);
            mineClassHeaderImageViewBig.setImageResource(R.drawable.default_ic_mine_class);
            curClassNameTextViewBig.setText("尚未加入任何班级");
            curClassNameTextView.setText("尚未加入任何班级");
            curClassPeopleCountTextView.setText("");
            curClassBaseLocate.setText("");
        } else {
            curClass = DataSupport.find(ClassObject.class,curClass.getId());
            mineClassHeaderImageView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (curClass.getHeaderPath().equals("")) {
                        mineClassHeaderImageView.setImageResource(R.drawable.default_ic_mine_class);
                    } else {
                        mineClassHeaderImageView.setImageResource(R.drawable.default_ic_mine_class);
                        FlareBitmapUtils.INSTANCE.loadBitmap(mineClassHeaderImageView, curClass.getHeaderPath());
                    }
                }
            }, 3000);
            if (curClass.getHeaderPath().equals("")) {
                mineClassHeaderImageViewBig.setImageResource(R.drawable.default_ic_mine_class);
            } else {
                FlareBitmapUtils.INSTANCE.loadBitmap(mineClassHeaderImageViewBig, curClass.getHeaderPath());
            }
            curClassNameTextViewBig.setText(curClass.getName() + " |");
            curClassNameTextView.setText(curClass.getName());
            curClassPeopleCountTextView.setText(curClass.getTotalPeopleCount() + "人");
            curClassBaseLocate.setText(curClass.getInSchool() + " | " + curClass.getInAcademy());

            //判断是否有权限使用通知/消息录制/公告
            List<ManagerObject> managers = DataSupport.where("classobject_id=?",String.valueOf(curClass.getId())).find(ManagerObject.class);
            String creator = curClass.getCreator();
            boolean isCreatorOrManager = false;
            if (creator.equals(BASE_GLOBAL_DATA.getUserID())) {
                isCreatorOrManager = true;
            } else {
                for (ManagerObject temp : managers) {
                    if (temp.getManagerID().equals(BASE_GLOBAL_DATA.getUserID())) {
                        isCreatorOrManager = true;
                        break;
                    }
                }
            }

            if (isCreatorOrManager) {
                functionFloatingMenu.setVisibility(View.VISIBLE);
            } else {
                functionFloatingMenu.setVisibility(View.GONE);
            }
        }

        //停止滚动，否则容易出现滚动时刚好清空了数据
        handler.sendEmptyMessage(AnnouncementHandler.MSG_KEEP_SILENT);
        if (announcementsFragments.size() > 0) {
            announcements.clear();
            announcementsFragments.clear();
        }
        if (curClass == null) {
            AnnouncementObject announcementObject = new AnnouncementObject();
            announcementObject.setTitle("尚未加入任何班级");
            announcementObject.setContent("请前往“添加班级”界面加入/创建新的班级~");
            announcementObject.setNameOfSender("吉祥物管家兽");
            announcementObject.setDeadline(new Date());
            announcements.add(announcementObject);

            //显示快速发送公告入口
        } else {
            fastSendAnnouncement.setVisibility(View.GONE);
            announcements = DataSupport.where("classobject_id=? and networkId is not null",String.valueOf(curClass.getId())).find(AnnouncementObject.class);
            if (announcements.size() == 0) {
                fastSendAnnouncement.setVisibility(View.VISIBLE);
                AnnouncementObject announcementObject = new AnnouncementObject();
                announcementObject.setTitle("快快来发表公告");
                announcementObject.setContent("本班群尚未存在任何公告~");
                announcementObject.setNameOfSender("吉祥物管家兽");
                announcementObject.setDeadline(null);
                announcements.add(announcementObject);
            }


            //获取最新的班级公告数据
            AVQuery<AVObject> announcementsQuery = new AVQuery<>("CMAnnouncement");
            announcementsQuery.whereEqualTo("inClassId",curClass.getClassID());
            announcementsQuery.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    if (e == null) {
                        if (list.size() > 0) {
                            List<String> toDeleteObjects = new ArrayList<>();
                            List<String> toAddObjects = new ArrayList<>();

                            for (AnnouncementObject object : announcements) {
                                if (object.getNetworkId() == null) {
                                    DataSupport.delete(AnnouncementObject.class,object.getId());
                                    continue;
                                }
                                toDeleteObjects.add(object.getNetworkId());
                            }

                            for (AVObject object : list) {
                                toAddObjects.add(object.getObjectId());
                            }

                            //排除相同点
                            int size = toDeleteObjects.size();
                            List<String> buffer = new ArrayList<>();
                            for (int i = 0;i < size;i++) {
                                String temp = toDeleteObjects.get(i);
                                if (toAddObjects.contains(temp)) {
                                    toAddObjects.remove(temp);
                                    buffer.add(temp);
                                }
                            }
                            for (String delete : buffer) {
                                toDeleteObjects.remove(delete);
                            }

                            //toDeleteObjects 剩下的就是要被删除的
                            for (String id : toDeleteObjects) {
                                DataSupport.deleteAll(AnnouncementObject.class,"networkId=?",id);
                                hasNewAnnouncement = true;
                            }

                            //toAddObjects 剩下的就是要被添加的
                            for (String id : toAddObjects) {
                                for (AVObject object : list) {
                                    if (object.getObjectId().equals(id)) {
                                        if (!object.getDate("deadLine").after(TODAY)) {
                                            continue;
                                        }
                                        AnnouncementObject announcementObject = new AnnouncementObject();
                                        announcementObject.setNetworkId(object.getObjectId());
                                        announcementObject.setDeadline(object.getDate("deadLine"));
                                        announcementObject.setInClass(curClass);
                                        announcementObject.setNameOfSender(object.getString("nameOfSender"));
                                        announcementObject.setTitle(object.getString("title"));
                                        announcementObject.setContent(object.getString("content"));
                                        announcementObject.save();
                                        hasNewAnnouncement = true;
                                    }
                                }
                            }
                        } else {
                            //如果网络上已经没有了公告数据，直接删除所有的本地数据
                            for (AnnouncementObject object : announcements) {
                                DataSupport.delete(AnnouncementObject.class,object.getId());
                                hasNewAnnouncement = true;
                            }
                        }
                    } else {
                        e.printStackTrace();
                    }
                }
            });

        }

        for (AnnouncementObject temp : announcements) {
            if (temp.getDeadline() != null && TODAY.after(temp.getDeadline())) {
                DataSupport.delete(AnnouncementObject.class,temp.getId());
            } else {
                announcementsFragments.add(AnnouncementsFragment.newInstance(temp,curClass));
            }
        }
        pagerAdapter.notifyDataSetChanged();

        announcementsViewPager.post(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessageDelayed(AnnouncementHandler.MSG_UPDATE_IMAGE, 500);
            }
        });


        if (curClass != null) {
            recentMessages.clear();
            recentMessages.addAll(DataSupport.where("classobject_id=? and userobject_id=?",
                    String.valueOf(curClass.getId()),String.valueOf(mineObject.getId())).
                    find(RecentMessageObject.class));
            Collections.sort(recentMessages);
            chattingAdapter.notifyDataSetChanged();
        }

        new DataInitTask().execute();

    }

    //后台处理高能耗数据初始化工作
    private class DataInitTask extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //检查最新的其他班级的消息
            List<ClassObject> otherClasses = DataSupport.where("classID<>?",MainActivity.BASE_GLOBAL_DATA.getCurClassID()).find(ClassObject.class);
            boolean hasNoRead = false;
            for (int i = 0;i < otherClasses.size();i++) {
                List<RecentMessageObject> recentMessageObjects = DataSupport.where("classobject_id=? and noReadNumber>0 and userobject_id=?",
                        String.valueOf(otherClasses.get(i).getId()),String.valueOf(mineObject.getId())).
                        order("time desc").
                        find(RecentMessageObject.class);
                int size = recentMessageObjects.size();
                for (int j = 0;j < size;j++) {
                    if (recentMessageObjects.get(j).getNoReadNumber() > 0) {
                        hasNoRead = true;
                        break;
                    }
                }
                if (hasNoRead) {
                    break;
                }
            }

            return hasNoRead;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                findViewById(R.id.iv_has_no_read).setVisibility(View.VISIBLE);
                findViewById(R.id.iv_has_no_read_drawer).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.iv_has_no_read).setVisibility(View.GONE);
                findViewById(R.id.iv_has_no_read_drawer).setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        announcementsViewPager.post(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(AnnouncementHandler.MSG_KEEP_SILENT);
            }
        });
    }

    private class TestPagerAdapter extends FragmentPagerAdapter
    {
        List<AnnouncementsFragment> items;

        public TestPagerAdapter(FragmentManager fm,List<AnnouncementsFragment> items)
        {
            super(fm);
            this.items = items;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public AnnouncementsFragment getItem(int position) {
            AnnouncementObject announcementObject;
            int size = items.size();
            if (size > 0) {
                position %= items.size();
                announcementObject = items.get(position).getObject();

                //直接提取的话将会导致内部的list存在相同tag的fragment，所以需要重新分配
                items.set(position, AnnouncementsFragment.newInstance(announcementObject, curClass));
                return items.get(position);
            } else {
                return AnnouncementsFragment.newInstance(new AnnouncementObject(), curClass);
            }
        }

    }

    //轮播关键Handler
    private static class AnnouncementHandler extends Handler{

        /**
         * 请求更新显示的View。
         */
        protected static final int MSG_UPDATE_IMAGE  = 1;
        /**
         * 请求暂停轮播。
         */
        protected static final int MSG_KEEP_SILENT   = 2;
        /**
         * 请求恢复轮播。
         */
        protected static final int MSG_BREAK_SILENT  = 3;
        /**
         * 记录最新的页号，当用户手动滑动时需要记录新页号，否则会使轮播的页面出错。
         * 例如当前如果在第一页，本来准备播放的是第二页，而这时候用户滑动到了末页，
         * 则应该播放的是第一页，如果继续按照原来的第二页播放，则逻辑上有问题。
         */
        protected static final int MSG_PAGE_CHANGED  = 4;

        //轮播间隔时间
        protected static final long MSG_DELAY = 3000;

        //使用弱引用避免Handler泄露.这里的泛型参数可以不是Activity，也可以是Fragment等
        private WeakReference<MainActivity> weakReference;
        private int currentItem = 0;

        protected AnnouncementHandler(WeakReference<MainActivity> wk){
            weakReference = wk;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = weakReference.get();
            if (activity == null){
                return ;
            }
            //检查消息队列并移除未发送的消息，这主要是避免在复杂环境下消息出现重复等问题。
            if (activity.handler.hasMessages(MSG_UPDATE_IMAGE)){
                activity.handler.removeMessages(MSG_UPDATE_IMAGE);
            }
            switch (msg.what) {
                case MSG_UPDATE_IMAGE:
                    int size = activity.announcements.size();
                    if (size >= 2) {
                        currentItem++;
                        activity.announcementsViewPager.setCurrentItem(currentItem);
                        //准备下次播放
                        activity.handler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
                    } else {
                        if (hasNewAnnouncement) {
                            currentItem++;
                            activity.announcementsViewPager.setCurrentItem(currentItem);
                            currentItem++;
                            activity.announcementsViewPager.setCurrentItem(currentItem);
                            hasNewAnnouncement = false;
                        }
                    }
                    break;
                case MSG_KEEP_SILENT:
                    //只要不发送消息就暂停了
                    break;
                case MSG_BREAK_SILENT:
                    activity.handler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
                    break;
                case MSG_PAGE_CHANGED:
                    //记录当前的页号，避免播放的时候页面显示不正确。
                    currentItem = msg.arg1;
                    break;
                default:
                    break;
            }
        }
    }

    private class ChattingAdapter extends BaseAdapter {

        private List<RecentMessageObject> items;

        private Context context;

        public ChattingAdapter(Context context, List<com.flaremars.classmanagers.model.RecentMessageObject> items) {
            this.context = context;

            this.items = items;
        }
        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public com.flaremars.classmanagers.model.RecentMessageObject getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
//            public RoundImageView roundImageView;

            public ImageView headImage;

            public TextView nameTextView;

            public TextView contentTextView;

            public TextView noReadNumTextView;

            public TextView timeTextView;

            public LinearLayout llDelete;

            public LinearLayout llUp;

            public FrameLayout noReadNumberBG;

            public ImageView imgUpDown;

            public TextView upDownTextView;

            public ImageView hasBeenZhidingImage;

//            public MyRippleView rippleView;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            final RecentMessageObject item = getItem(position);
            SlideView slideView = (SlideView) convertView;
            if (slideView == null) {

                View contentView = View.inflate(context,R.layout.item_chatting_no_ripple,null);
                slideView = new SlideView(MainActivity.this);
                slideView.setBackgroundResource(R.drawable.bg_recent_chatting_item);
                slideView.setContentView(contentView);
                slideView.setOnSlideListener(MainActivity.this);

                holder = new ViewHolder();
                holder.headImage = (ImageView) slideView.findViewById(R.id.item_chatting_img);
                holder.contentTextView = (TextView) slideView.findViewById(R.id.item_chatting_content);
                holder.nameTextView = (TextView) slideView.findViewById(R.id.item_chatting_name);
                holder.noReadNumTextView = (TextView) slideView.findViewById(R.id.item_chatting_no_read);
                holder.timeTextView = (TextView) slideView.findViewById(R.id.item_chatting_last_time);
                holder.llDelete = (LinearLayout) slideView.findViewById(R.id.slide_ll_delete);
                holder.llUp = (LinearLayout) slideView.findViewById(R.id.slide_ll_up);
                holder.noReadNumberBG = (FrameLayout) slideView.findViewById(R.id.fl_chatting_no_read_bg);
                holder.upDownTextView = (TextView) slideView.findViewById(R.id.slide_up);
                holder.imgUpDown = (ImageView) slideView.findViewById(R.id.slide_up_down_img);
                holder.hasBeenZhidingImage = (ImageView) slideView.findViewById(R.id.iv_has_been_zhiding);
                slideView.setTag(holder);
            } else {
                holder = (ViewHolder) slideView.getTag();
            }

            switch (item.getType()) {
                case RecentMessageObject.TYPE_NOTICE_GROUP:
                    if (item.getNoReadNumber() > 0) {
                        holder.nameTextView.setTextColor(Color.rgb(237,49,63));
                    } else {
                        holder.nameTextView.setTextColor(Color.BLACK);
                    }
                    break;
                case RecentMessageObject.TYPE_EXCEL:
                    ExcelObject temp = DataSupport.find(ExcelObject.class,item.getTargetID());
                    if (temp != null && temp.getAnswers().equals("")) {
                        holder.nameTextView.setTextColor(Color.rgb(237,49,63));
                    } else {
                        if (item.getNoReadNumber() > 0) {
                            ContentValues cv = new ContentValues();
                            cv.put("noReadNumber", 0);
                            DataSupport.update(RecentMessageObject.class, cv, item.getId());
                        }
                        holder.nameTextView.setTextColor(Color.BLACK);
                    }
                    break;
                default:
                    holder.nameTextView.setTextColor(Color.BLACK);
            }

            item.slideView = slideView;
            item.slideView.shrink();

            holder.timeTextView.setText(DATE_FORMAT.format(item.getTime()));

            if (item.getNoReadNumber() == 0 || item.getType() == RecentMessageObject.TYPE_EXCEL) {
                holder.noReadNumberBG.setVisibility(View.INVISIBLE);
            } else {
                holder.noReadNumberBG.setVisibility(View.VISIBLE);
                holder.noReadNumTextView.setText(item.getNoReadNumber()+"");
            }

            holder.nameTextView.setText(item.getName());

            if (item.getType() == RecentMessageObject.TYPE_EXCEL ||
                    item.getType() == RecentMessageObject.TYPE_NOTICE) {
                holder.contentTextView.setText("");
            } else {
                holder.contentTextView.setText(item.getContent());
            }
            Bitmap bitmap = null;
            switch (item.getType()) {
                case RecentMessageObject.TYPE_NOTICE_GROUP:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_msg);
                    break;
                case RecentMessageObject.TYPE_GROUP:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_group);
                    break;
                case RecentMessageObject.TYPE_PERSON:
                    bitmap = null;
                    break;
                case RecentMessageObject.TYPE_EXCEL:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_circle_excel);
                    break;
                case RecentMessageObject.TYPE_NOTICE:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_notice);
                    break;
                default:
                    break;
            }
            if (item.getType() != RecentMessageObject.TYPE_PERSON) {
                holder.headImage.setImageBitmap(bitmap);
            } else {
                if (item.getImgPath().equals("")) {
                    holder.headImage.setImageResource(R.drawable.default_ic_contact);
                } else {
                    FlareBitmapUtils.INSTANCE.loadBitmap(holder.headImage,item.getImgPath());
                }
            }

            holder.llDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    items.remove(item);
                    DataSupport.delete(RecentMessageObject.class, item.getId());
                    ChattingAdapter.this.notifyDataSetChanged();
                }
            });
            holder.llUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues cv = new ContentValues();
                    cv.put("isAlwaysTop",!item.isAlwaysTop());
                    item.setAlwaysTop(!item.isAlwaysTop());

                    DataSupport.update(RecentMessageObject.class,cv,item.getId());
                    Collections.sort(items);
                    ChattingAdapter.this.notifyDataSetChanged();
                }
            });

            if (item.isAlwaysTop()) {
                holder.imgUpDown.setImageResource(R.drawable.ic_cancelzhiding);
                holder.upDownTextView.setText("取消置顶");
                holder.hasBeenZhidingImage.setVisibility(View.VISIBLE);
            } else {
                holder.imgUpDown.setImageResource(R.drawable.ic_up);
                holder.upDownTextView.setText("置顶");
                holder.hasBeenZhidingImage.setVisibility(View.INVISIBLE);
            }

//            holder.rippleView.setOnAnimationEndListener(new MyRippleView.OnAnimationEndListener() {
//                @Override
//                public void onAnimationEnd() {
//
//                }
//            });
            return slideView;
        }
    }

    private void updateFlexibleSpaceText(final int scrollY) {
        ViewHelper.setTranslationY(headerView,-scrollY);

        float limit = flexibleSpaceImageHeight - smallHeaderHeight;
        float scrollYOffset = Math.min(limit,scrollY);
        float percent = scrollYOffset / limit;

        ViewHelper.setAlpha(smallHeaderView,percent);

    }

    public static SlideView getSlideView() {
        return mLastSlideViewWithStatusOn;
    }

    @Override
    public void onSlide(View view, int status) {
        if (mLastSlideViewWithStatusOn != null && mLastSlideViewWithStatusOn != view) {
            mLastSlideViewWithStatusOn.shrink();
        }

        if (status == SLIDE_STATUS_ON) {
            mLastSlideViewWithStatusOn = (SlideView) view;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        NotificationUtils.INSTANCE.setMode(1);
        ClassObject temp = curClass;
        if (!BASE_GLOBAL_DATA.getCurClassID().equals("")) {
            if (curClass == null ||
                    !BASE_GLOBAL_DATA.getCurClassID().equals(temp.getClassID())) {
                curClass = DataSupport.where("classID=?",BASE_GLOBAL_DATA.getCurClassID()).find(ClassObject.class).get(0);
            }
        }

        //消息列表长按对话框如果此前被单击了，就可能响应出长按，从而不是用户意愿地进行显示
        ContextMenuDialog dialog = ContextMenuDialog.getFunctionDialog(null, null,false);
        if (!dialog.isHidden()) {
            if (dialog.getDialog() != null) {
                dialog.getDialog().hide();
            }
        }
        reloadUI();
        //        startGetMessageService();
        receiver = new RecentMessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CMService.HAS_NEW_MESSAGES);
        filter.addAction(CMService.HAS_NEW_TASK);
        registerReceiver(receiver, filter);

    }

    private RecentMessageReceiver receiver;

    private class RecentMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
 //           Log.e("TAG", "onReceive messages~");
            if (curClass != null) {
                recentMessages.clear();
                recentMessages.addAll(DataSupport.where("classobject_id=? and userobject_id=?",
                        String.valueOf(curClass.getId()),String.valueOf(mineObject.getId())).
                        find(RecentMessageObject.class));
                Collections.sort(recentMessages);
                chattingAdapter.notifyDataSetChanged();
            }
            if (recentMessages == null) {
                recentMessages = new ArrayList<>();
            }
        }
    }

    private void startCMService() {
        Log.e("TAG","startCMService");
        if (!BASE_GLOBAL_DATA.getCurClassID().equals("")) {
            startService(new Intent(this, CMService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FlareBitmapUtils.INSTANCE.flushCache();
        NotificationUtils.INSTANCE.setMode(1);
//        Log.e("TAG","MainActivity -- onDestroy");
//        myClient.close(new AVIMClientCallback() {
//            @Override
//            public void done(AVIMClient avimClient, AVException e) {
//
//            }
//        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

}
