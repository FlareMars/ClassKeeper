package com.flaremars.classmanagers.uis;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.DataWrapper;
import com.flaremars.classmanagers.model.ExcelFeedbackObject;
import com.flaremars.classmanagers.model.ExcelModelObject;
import com.flaremars.classmanagers.model.ExcelTaskObject;
import com.flaremars.classmanagers.model.NoticeGroupMemberObject;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.utils.MessagePostUtil;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class CreateInfoCollectionFragment extends BaseFragment {

    private static final SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);

    private static final String TARGET = "param1";
    private static final String INIT_DATA = "param2";
    private static final String IS_POINT_TO_PERSON = "param3";

    private static final HashMap<Integer,String> POSITION2STRING = new HashMap<>(60);

    private String target;

    private String initData = "";

    private boolean isPointToPerson;

    private MyAdapter adapter;

    private List<TestObjectItem> items;

    private ListView contentListView;

    private EditText modelNameEditText;

    private RecyclerView contentRecyclerView;

    private RecyclerAdapter recyclerAdapter;

    private int curSize = 1;

    static {
        POSITION2STRING.put(0,"第一项");
        POSITION2STRING.put(1,"第二项");
        POSITION2STRING.put(2,"第三项");
        POSITION2STRING.put(3,"第四项");
        POSITION2STRING.put(4,"第五项");
        POSITION2STRING.put(5,"第六项");
        POSITION2STRING.put(6,"第七项");
        POSITION2STRING.put(7,"第八项");
        POSITION2STRING.put(8,"第九项");
        POSITION2STRING.put(9,"第十项");
        POSITION2STRING.put(10,"第十一项");
        POSITION2STRING.put(11,"第十二项");
        POSITION2STRING.put(12,"第十三项");
        POSITION2STRING.put(13,"第十四项");
        POSITION2STRING.put(14,"第十五项");
        POSITION2STRING.put(15,"第十六项");
        POSITION2STRING.put(16,"第十七项");
        POSITION2STRING.put(17,"第十八项");
        POSITION2STRING.put(18,"第十九项");
        POSITION2STRING.put(19,"第二十项");
        POSITION2STRING.put(20,"第二十一项");
        POSITION2STRING.put(21,"第二十二项");
        POSITION2STRING.put(22,"第二十三项");
        POSITION2STRING.put(23,"第二十四项");
        POSITION2STRING.put(24,"第二十五项");
        POSITION2STRING.put(25,"第二十六项");
        POSITION2STRING.put(26,"第二十七项");
        POSITION2STRING.put(27,"第二十八项");
        POSITION2STRING.put(28,"第二十九项");
        POSITION2STRING.put(29,"第三十项");
        POSITION2STRING.put(30,"第三十一项");
        POSITION2STRING.put(31,"第三十二项");
        POSITION2STRING.put(32,"第三十三项");
        POSITION2STRING.put(33,"第三十四项");
        POSITION2STRING.put(34,"第三十五项");
        POSITION2STRING.put(35,"第三十六项");
        POSITION2STRING.put(36,"第三十七项");
        POSITION2STRING.put(37,"第三十八项");
        POSITION2STRING.put(38,"第三十九项");
        POSITION2STRING.put(39,"第四十项");
        POSITION2STRING.put(40,"第四十一项");
        POSITION2STRING.put(41,"第四十二项");
        POSITION2STRING.put(42,"第四十三项");
        POSITION2STRING.put(43,"第四十四项");
        POSITION2STRING.put(44,"第四十五项");
        POSITION2STRING.put(45,"第四十六项");
        POSITION2STRING.put(46,"第四十七项");
        POSITION2STRING.put(47,"第四十八项");
        POSITION2STRING.put(48,"第四十九项");
        POSITION2STRING.put(49,"第五十项");
        POSITION2STRING.put(50,"第五十一项");
    }

    public static CreateInfoCollectionFragment newInstance(String target,String initData,boolean isPointToPerson) {
        CreateInfoCollectionFragment fragment = new CreateInfoCollectionFragment();
        Bundle args = new Bundle();
        args.putString(TARGET, target);
        args.putString(INIT_DATA,initData);
        args.putBoolean(IS_POINT_TO_PERSON,isPointToPerson);
        fragment.setArguments(args);
        return fragment;
    }

    public CreateInfoCollectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            target = getArguments().getString(TARGET);
            initData = getArguments().getString(INIT_DATA);
            isPointToPerson = getArguments().getBoolean(IS_POINT_TO_PERSON);
        }
    }

    @Override
    void backAction() {
        getContainerActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    String fragmentTitle() {
        return "新建信息录制";
    }

    @Override
    List<Integer> setActionViews() {
        return null;
    }

    private class TestObjectItem {
        private String content;

        private boolean isLastItem;

        private TestObjectItem(String content) {
            isLastItem = false;
            this.content = content;
        }

        private TestObjectItem() {
            isLastItem = true;
            this.content = "";
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isLastItem() {
            return isLastItem;
        }

        public void setIsLastItem(boolean isLastItem) {
            this.isLastItem = isLastItem;
        }
    }

//    private Set<View> Views = new HashSet<>();

    private int index = -1;

    private class MyAdapter extends BaseAdapter {

        private Context context;

        private List<TestObjectItem> items;

        private MyAdapter(Context context, List<TestObjectItem> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public TestObjectItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            public TextView headerTextView;

            public MaterialEditText contentEditText;

            public ImageView deleteButton;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final TestObjectItem item = getItem(position);
            final ViewHolder holder;

//            if (convertView == null) {
//                convertView = View.inflate(context,R.layout.item_info_collection_question,null);
//                holder = new ViewHolder();
//                holder.headerTextView = (TextView) convertView.findViewById(R.id.item_info_collection_question_header);
//                holder.contentEditText = (MaterialEditText) convertView.findViewById(R.id.item_info_collection_question_content);
//
//                holder.deleteButton = (ImageView) convertView.findViewById(R.id.item_info_collection_question_delete);
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
            convertView = View.inflate(context,R.layout.item_info_collection_question,null);
            holder = new ViewHolder();
            holder.headerTextView = (TextView) convertView.findViewById(R.id.item_info_collection_question_header);
            holder.contentEditText = (MaterialEditText) convertView.findViewById(R.id.item_info_collection_question_content);

            holder.deleteButton = (ImageView) convertView.findViewById(R.id.item_info_collection_question_delete);

            holder.headerTextView.setText(POSITION2STRING.get(position));
            holder.contentEditText.setText(item.getContent());
//            Views.add(holder.contentEditText);

            holder.contentEditText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            if (position == items.size() - 2) {
                                index= position + 1;
                                if (index == items.size() - 1) {
                                    items.get(position).setIsLastItem(false);
                                    items.get(index).setIsLastItem(false);
                                    items.add(new TestObjectItem());
                                    adapter.notifyDataSetChanged();
                                    contentListView.smoothScrollToPosition(items.size() - 1);
                                }
                            }
                        }
                    }
                    return false;
                }
            });

            holder.contentEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    items.get(position).setContent(s.toString());
                }
            });

//            holder.contentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//
//                    if (hasFocus) {
//                        //如果是最后一个输入框，则马上生成一个新的输入框
//                        if (items.size() >= 49) {
//                            return;
//                        }
//                        if (position == items.size() - 1) {
//                            items.get(position).setIsLastItem(false);
//                            items.add(new TestObjectItem());
//                            for (View temp : Views) {
//                                temp.setFocusable(false);
//                                temp.setFocusableInTouchMode(false);
//                            }
////                            holder.contentEditText.setFocusable(true);
////                            holder.contentEditText.setFocusableInTouchMode(true);
////                            holder.contentEditText.requestFocus();
////                            Log.e("TAG", modelNameEditText.getRootView().findFocus().toString());
//                            MyAdapter.this.notifyDataSetChanged();
////                            Log.e("TAG", modelNameEditText.getRootView().findFocus().toString());
//                            holder.contentEditText.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    holder.contentEditText.setFocusable(true);
//                                    holder.contentEditText.setFocusableInTouchMode(true);
//                                    holder.contentEditText.requestFocus();
//                                    holder.contentEditText.postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            openSoftInput(holder.contentEditText);
//                                        }
//                                    },100);
//                                    for (View temp : Views) {
//                                        temp.setFocusable(true);
//                                        temp.setFocusableInTouchMode(true);
//                                    }
//                                }
//                            }, 100);
//                            contentListView.smoothScrollToPosition(items.size() - 1);
//                        }
//                    } else {
//                        items.get(position).setContent(holder.contentEditText.getText().toString());
//                    }
//
//                }
//            });

            holder.contentEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        Log.e("TAG",position + " " + items.size());
                        index= position;
                        if (index == items.size() - 1) {
                            items.get(position).setIsLastItem(false);
                            items.add(new TestObjectItem());
                            adapter.notifyDataSetChanged();
                            contentListView.smoothScrollToPosition(items.size() - 1);
                        }
                    }
                    return false;
                }
            });

            if (!item.isLastItem) {
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        items.remove(items.get(position));
                        MyAdapter.this.notifyDataSetChanged();
                        if (items.size() == 1) {
                            modelNameEditText.setFocusable(true);
                            modelNameEditText.setFocusableInTouchMode(true);
                            modelNameEditText.requestFocus();
                        }
                    }
                });
            } else {
                holder.deleteButton.setVisibility(View.INVISIBLE);
            }

            holder.contentEditText.clearFocus();
            if(index!= -1 && index == position) {
                holder.contentEditText.setFocusable(true);
                holder.contentEditText.setFocusableInTouchMode(true);
                holder.contentEditText.requestFocus();
            }
            return convertView;
        }
    }

    private void openSoftInput(View view) {
//        View view = getContainerActivity().getWindow().peekDecorView();
//        if (view == null) {
//            Log.e("TAG","view == null");
//            InputMethodManager imm = (InputMethodManager) getContainerActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//        } else {
//            Log.e("TAG","view != null");
//        }
        if (view.hasFocus()) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContainerActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_info_collection, container, false);
        contentListView = (ListView) view.findViewById(R.id.fragment_create_info_collection_content);
//        contentListView.setFocusable(false);
//        contentListView.setFocusableInTouchMode(false);
//        contentRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_create_info_collection_content);
        final TextView confirmBtn = (TextView) view.findViewById(R.id.fragment_create_info_collection_confirm);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent data = new Intent();
                String modelName = modelNameEditText.getText().toString();
                StringBuilder sb = new StringBuilder();
                for (TestObjectItem item : items) {
                    if (!item.getContent().equals("")) {
                        sb.append(item.getContent());
                        sb.append("_");
                    }
                }
                if (sb.length() == 0) {
                    Toast.makeText(getContainerActivity(), "内容不能为空哦~", Toast.LENGTH_LONG).show();
                    return;
                }
                sb.deleteCharAt(sb.length() - 1);
                String questions = sb.toString();
                if (modelName.equals("") || questions.equals("")) {
                    Toast.makeText(getContainerActivity(), "标题不能为空哦~", Toast.LENGTH_LONG).show();
                    return;
                }
                data.putExtra("data", questions);
                data.putExtra("model_name", modelName);

                List<ExcelModelObject> models = DataSupport.findAll(ExcelModelObject.class);
                int targetModelId = 0;
                for (ExcelModelObject object : models) {
                    if (object.getQuestions().equals(questions)) {
                        targetModelId = object.getId();
                        break;
                    }
                }
                //就是一个不同于其他的模板，需要被保存
                if (targetModelId == 0) {
                    ExcelModelObject modelObject = new ExcelModelObject();
                    modelObject.setName(modelName);
                    modelObject.setQuestions(questions);
                    modelObject.save();
                    targetModelId = modelObject.getId();
                }

                data.putExtra("model_id", targetModelId);

                ExcelTaskObject excelTaskObject = new ExcelTaskObject();
                if (!isPointToPerson) {
                    NoticeGroupObject targetNoticeGroup = DataSupport.find(NoticeGroupObject.class, Long.valueOf(target));
                    List<NoticeGroupMemberObject> targets = targetNoticeGroup.getMemberList();
                    final List<String> targetIds = new ArrayList<>();

                    excelTaskObject.setInNoticeGroup(targetNoticeGroup);
                    Date now = new Date();
                    excelTaskObject.setExcelId(DATA_FORMAT.format(now));
                    excelTaskObject.setTime(now);
                    excelTaskObject.setName(modelName);
                    excelTaskObject.setNewFeedback(false);
                    excelTaskObject.setQuestions(questions);
                    excelTaskObject.save();
                    data.putExtra("excelTaskObject_id", excelTaskObject.getId());

                    //由于进入通知组界面的不一定是只能进入当前班级通知组，所以这里要根据noticegroupobject获取真正的班级
                    Cursor classIdCursor = DataSupport.findBySQL("select classobject_id from noticegroupobject where id=?",
                            String.valueOf(targetNoticeGroup.getId()));
                    ClassObject classObject;
                    if (classIdCursor != null && classIdCursor.moveToFirst()) {
                        classObject = DataSupport.find(ClassObject.class, classIdCursor.getLong(0));
                    } else {
                        Log.e("TAG", "出错，查找班级失败");
                        return;
                    }

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
                        msgContent.put(MessageConst.CONTENT_IN_CLASS, classObject.getClassID());
                        msgContent.put(MessageConst.CONTENT_TIME, excelTaskObject.getTime().getTime());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    DataWrapper wrapper = new DataWrapper(
                            MainActivity.BASE_GLOBAL_DATA.getUserID(),
                            classObject.getClassID(),
                            "",
                            targetIds,
                            msgContent
                    );

                    updateRecentMessage(classObject, excelTaskObject.getName(), targetNoticeGroup);

                    int messageId = MessagePostUtil.INSTANCE.wannaPostMessage(wrapper);
                    Log.e("TAG", "messageId = " + messageId);

                }

                //如果是单聊的话，直接通过activity返回给对方即可，不作记录
                getContainerActivity().setResult(Activity.RESULT_OK, data);
                getContainerActivity().finish();

            }
        });

        modelNameEditText = (EditText) view.findViewById(R.id.fragment_create_info_collection_name);
        modelNameEditText.requestFocus();
        modelNameEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                openSoftInput(modelNameEditText);
            }
        },100);
//        openSoftInput(modelNameEditText);
//        Views.add(modelNameEditText);
//        Views.add(confirmBtn);

        items = new ArrayList<>();
            if (!initData.equals("")) {
                String [] questions = initData.split("_");
            for (String question : questions) {
                items.add(new TestObjectItem(question));
            }
        }

        items.add(new TestObjectItem());
        adapter = new MyAdapter(getContainerActivity(),items);
        contentListView.setAdapter(adapter);
//        View footerPaddingView = new View(getContainerActivity());
//        ListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,140);
//        footerPaddingView.setLayoutParams(layoutParams);
//        contentListView.addFooterView(footerPaddingView);
//        contentRecyclerView.setLayoutManager(new LinearLayoutManager(getContainerActivity(), LinearLayoutManager.VERTICAL, false));
//        contentRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        contentRecyclerView.setHasFixedSize(false);
//        recyclerAdapter = new RecyclerAdapter(getContainerActivity(),items);
//        contentRecyclerView.setAdapter(recyclerAdapter);
        return view;
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        private List<TestObjectItem> items;

        private Context context;

        public RecyclerAdapter (Context context,List<TestObjectItem> items) {
            this.items = items;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_info_collection_question, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder,final int position) {
            TestObjectItem item = items.get(position);

            holder.headerTextView.setText(POSITION2STRING.get(position));
            holder.contentEditText.setText(item.getContent());
//            Views.add(holder.contentEditText);

            holder.contentEditText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            items.get(position).setContent(holder.contentEditText.getText().toString());
                        }
                    }
                    return false;
                }
            });

            holder.contentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    if (hasFocus) {
                        //如果是最后一个输入框，则马上生成一个新的输入框
                        if (items.size() >= 49) {
                            return;
                        }
                        if (position == items.size() - 1) {
                            items.get(position).setIsLastItem(false);
                            items.add(new TestObjectItem());
//                            for (View temp : Views) {
//                                temp.setFocusable(false);
//                                temp.setFocusableInTouchMode(false);
//                            }
//                            holder.contentEditText.setFocusable(true);
//                            holder.contentEditText.setFocusableInTouchMode(true);
//                            holder.contentEditText.requestFocus();
//                            Log.e("TAG", modelNameEditText.getRootView().findFocus().toString());
                            RecyclerAdapter.this.notifyDataSetChanged();
//                            RecyclerAdapter.this.notifyItemInserted(position+1);
//                            Log.e("TAG", modelNameEditText.getRootView().findFocus().toString());
                            holder.contentEditText.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    holder.contentEditText.requestFocus();
                                }
                            },100);
//                            holder.contentEditText.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    holder.contentEditText.setFocusable(true);
//                                    holder.contentEditText.setFocusableInTouchMode(true);
//                                    holder.contentEditText.requestFocus();
//                                    holder.contentEditText.postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            openSoftInput(holder.contentEditText);
//                                        }
//                                    },100);
//                                    for (View temp : Views) {
//                                        temp.setFocusable(true);
//                                        temp.setFocusableInTouchMode(true);
//                                    }
//                                }
//                            }, 100);
//                            contentListView.smoothScrollToPosition(items.size() - 1);
                        }
                    } else {
                        items.get(position).setContent(holder.contentEditText.getText().toString());
                    }

                }
            });

            if (!item.isLastItem) {
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        items.remove(position);
                        RecyclerAdapter.this.notifyDataSetChanged();
                        if (items.size() == 1) {
                            modelNameEditText.setFocusable(true);
                            modelNameEditText.setFocusableInTouchMode(true);
                            modelNameEditText.requestFocus();
                        }
                    }
                });
            } else {
                holder.deleteButton.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView headerTextView;

            public MaterialEditText contentEditText;

            public ImageView deleteButton;

            public ViewHolder(View v){
                super(v);
                headerTextView = (TextView) v.findViewById(R.id.item_info_collection_question_header);
                contentEditText = (MaterialEditText) v.findViewById(R.id.item_info_collection_question_content);
                deleteButton = (ImageView) v.findViewById(R.id.item_info_collection_question_delete);
            }
        }
    }

    //TODO 重构
    /*创建通知组的最近消息*/
    private void updateRecentMessage(ClassObject classObject,String content,NoticeGroupObject noticeGroupObject) {
        List<UserObject> temp = DataSupport.where("userId=?",MainActivity.BASE_GLOBAL_DATA.getUserID()).find(UserObject.class);
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
