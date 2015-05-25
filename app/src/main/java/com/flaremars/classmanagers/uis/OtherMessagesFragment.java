package com.flaremars.classmanagers.uis;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.ChattingActivity;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.mainui.NoticeGroupActivity;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.DataWrapper;
import com.flaremars.classmanagers.model.ExcelObject;
import com.flaremars.classmanagers.model.NoticeObject;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.utils.BitmapUtils;
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

public class OtherMessagesFragment extends BaseFragment {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);

    private List<GroupItem> items;

    private UserObject mineObject;

    private MyAdapter adapter;

    private List<ClassObject> otherClasses;

    public OtherMessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_messages, container, false);
        ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.elv_fragment_other_message_content);

        mineObject = DataSupport.where("userId=?",MainActivity.BASE_GLOBAL_DATA.getUserID()).find(UserObject.class).get(0);

        otherClasses = DataSupport.where("classID<>?",MainActivity.BASE_GLOBAL_DATA.getCurClassID()).find(ClassObject.class);
        items = new ArrayList<>();
        adapter = new MyAdapter(getContainerActivity(),items);
        listView.setAdapter(adapter);

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ClassObject targetClass = items.get(groupPosition).getTargetClass();
                RecentMessageObject item = items.get(groupPosition).getMessages().get(childPosition);

                int type = item.getType();
                if (type != RecentMessageObject.TYPE_EXCEL) {
                    ContentValues cv = new ContentValues();
                    cv.put("noReadNumber", 0);
                    DataSupport.update(RecentMessageObject.class, cv, item.getId());
                }
                switch (type) {
                    case RecentMessageObject.TYPE_NOTICE_GROUP:
                        Intent intent3 = new Intent(getContainerActivity(), NoticeGroupActivity.class);
                        intent3.putExtra("target_notice_group", item.getTargetID());
                        intent3.putExtra("is_show_input_layout", false);
                        startActivity(intent3);
                        break;
                    case RecentMessageObject.TYPE_PERSON:
                        Intent intent = new Intent(getContainerActivity(), ChattingActivity.class);
                        intent.putExtra("target", item.getTargetID());
                        intent.putExtra("is_group", false);
                        intent.putExtra("targetclass_id",targetClass.getClassID());
                        startActivity(intent);
                        break;
                    case RecentMessageObject.TYPE_GROUP:
                        Intent intent2 = new Intent(getContainerActivity(), ChattingActivity.class);
                        intent2.putExtra("target", item.getTargetID());
                        intent2.putExtra("is_group", true);
                        intent2.putExtra("targetclass_id",targetClass.getClassID());
                        startActivity(intent2);
                        break;
                    case RecentMessageObject.TYPE_EXCEL:
                        int targetId = item.getTargetID();
                        Intent intent1 = new Intent(getContainerActivity(), ContainerActivity.class);
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

                        Intent openDetailIntent = new Intent(getContainerActivity(), ContainerActivity.class);
                        Bundle bundle1 = new Bundle();
                        bundle1.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_NOTICE_DETAIL);
                        bundle1.putString("nameOfSender", noticeObject.getNameOfSender());
                        bundle1.putString("time", String.valueOf(noticeObject.getTime().getTime()));
                        bundle1.putString("content", noticeObject.getContent());
                        openDetailIntent.putExtras(bundle1);
                        startActivity(openDetailIntent);

                        if (item.getNoReadNumber() == 0) {
                            return true;
                        } else {
                            List<String> targetIds = new ArrayList<>();
                            targetIds.add(noticeObject.getIdOfSender());
                            final JSONObject msgContent = new JSONObject();
                            try {
                                msgContent.put(MessageConst.MSG_TYPE, MessageConst.NEW_NOTICE_FEEDBACK);
                                msgContent.put(MessageConst.CONTENT_FROM_ID, MainActivity.BASE_GLOBAL_DATA.getUserID());
                                msgContent.put(MessageConst.CONTENT_TARGET_ID, noticeObject.getNoticeId());
                                msgContent.put(MessageConst.CONTENT_IN_CLASS, targetClass.getClassID());
                                msgContent.put(MessageConst.CONTENT_TIME, new Date().getTime());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            DataWrapper wrapper = new DataWrapper(
                                    MainActivity.BASE_GLOBAL_DATA.getUserID(),
                                    targetClass.getClassID(),
                                    "",
                                    targetIds,
                                    msgContent
                            );

                            int messageId = MessagePostUtil.INSTANCE.wannaPostMessage(wrapper);
                            Log.e("TAG", "messageId = " + messageId);
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        items.clear();
        adapter.notifyDataSetChanged();

        int classSize = otherClasses.size();
        for (int i = 0;i < classSize;i++) {
            List<RecentMessageObject> recentMessageObjects = DataSupport.where("classobject_id=? and noReadNumber>0 and userobject_id=?",
                    String.valueOf(otherClasses.get(i).getId()),String.valueOf(mineObject.getId())).
                    find(RecentMessageObject.class);
            int totalNoReadNum = 0;
            for (int j = 0;j < recentMessageObjects.size();j++) {
                totalNoReadNum += recentMessageObjects.get(j).getNoReadNumber();
            }
            if (totalNoReadNum > 0) {
                items.add(new GroupItem(otherClasses.get(i).getName(), totalNoReadNum, recentMessageObjects, otherClasses.get(i)));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class GroupItem {
        private String name;

        private int noReadNum;

        private List<RecentMessageObject> messages;

        private ClassObject targetClass;

        private GroupItem(String name, int noReadNum, List<RecentMessageObject> messages,ClassObject targetClass) {
            this.name = name;
            this.noReadNum = noReadNum;
            this.messages = messages;
            this.targetClass = targetClass;
        }

        public ClassObject getTargetClass() {
            return targetClass;
        }

        public void setTargetClass(ClassObject targetClass) {
            this.targetClass = targetClass;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNoReadNum() {
            return noReadNum;
        }

        public void setNoReadNum(int noReadNum) {
            this.noReadNum = noReadNum;
        }

        public List<RecentMessageObject> getMessages() {
            return messages;
        }

        public void setMessages(List<RecentMessageObject> messages) {
            this.messages = messages;
        }
    }

    private class MyAdapter extends BaseExpandableListAdapter {

        private Context context;

        private List<GroupItem> items;

        private MyAdapter(Context context, List<GroupItem> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return items.get(groupPosition).getMessages().size();
        }

        @Override
        public GroupItem getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public RecentMessageObject getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).getMessages().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return getGroupId(groupPosition) * 10 + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        private class GroupViewHolder {
            public TextView nameTextView;

            public TextView noReadNumTextView;

            public FrameLayout noReadBg;
        }

        private class ChildViewHolder {
            public ImageView headImage;

            public TextView nameTextView;

            public TextView contentTextView;

            public TextView noReadNumTextView;

            public TextView timeTextView;

            public FrameLayout noReadNumberBG;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupItem item = getGroup(groupPosition);
            GroupViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_other_message_main,null);
                viewHolder = new GroupViewHolder();
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_other_message_main_name);
                viewHolder.noReadNumTextView = (TextView) convertView.findViewById(R.id.item_other_message_main_num);
                viewHolder.noReadBg = (FrameLayout) convertView.findViewById(R.id.fl_no_read_bg);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (GroupViewHolder) convertView.getTag();
            }

//            if (item.getNoReadNum() > 0) {
//                viewHolder.noReadBg.setVisibility(View.VISIBLE);
//                viewHolder.noReadNumTextView.setText(item.getNoReadNum() + "");
//            } else {
//                viewHolder.noReadBg.setVisibility(View.GONE);
//            }
            viewHolder.nameTextView.setText(item.getName());
            viewHolder.noReadNumTextView.setText(item.getNoReadNum() + "");
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            RecentMessageObject item = getChild(groupPosition,childPosition);
            ChildViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ChildViewHolder();
                convertView = View.inflate(context,R.layout.item_chatting,null);
                viewHolder.headImage = (ImageView) convertView.findViewById(R.id.item_chatting_img);
                viewHolder.contentTextView = (TextView) convertView.findViewById(R.id.item_chatting_content);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_chatting_name);
                viewHolder.noReadNumTextView = (TextView) convertView.findViewById(R.id.item_chatting_no_read);
                viewHolder.timeTextView = (TextView) convertView.findViewById(R.id.item_chatting_last_time);
                viewHolder.noReadNumberBG = (FrameLayout) convertView.findViewById(R.id.fl_chatting_no_read_bg);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ChildViewHolder) convertView.getTag();
            }

            switch (item.getType()) {
                case RecentMessageObject.TYPE_NOTICE:
                case RecentMessageObject.TYPE_NOTICE_GROUP:
                    if (item.getNoReadNumber() > 0) {
                        viewHolder.nameTextView.setTextColor(Color.rgb(237,49,63));
                    } else {
                        viewHolder.nameTextView.setTextColor(Color.BLACK);
                    }
                    break;
                case RecentMessageObject.TYPE_EXCEL:
                    ExcelObject temp = DataSupport.find(ExcelObject.class,item.getTargetID());
                    if (temp != null && temp.getAnswers().equals("")) {
                        viewHolder.nameTextView.setTextColor(Color.rgb(237,49,63));
                    } else {
                        if (item.getNoReadNumber() > 0) {
                            ContentValues cv = new ContentValues();
                            cv.put("noReadNumber", 0);
                            DataSupport.update(RecentMessageObject.class, cv, item.getId());
                        }
                        viewHolder.nameTextView.setTextColor(Color.BLACK);
                    }
                    break;
                default:
                    viewHolder.nameTextView.setTextColor(Color.BLACK);
            }

            viewHolder.timeTextView.setText(DATE_FORMAT.format(item.getTime()));

            if (item.getNoReadNumber() == 0 || item.getType() == RecentMessageObject.TYPE_EXCEL ||
                    item.getType() == RecentMessageObject.TYPE_NOTICE) {
                viewHolder.noReadNumberBG.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.noReadNumberBG.setVisibility(View.VISIBLE);
                viewHolder.noReadNumTextView.setText(item.getNoReadNumber()+"");
            }

            viewHolder.nameTextView.setText(item.getName());

            if (item.getType() == RecentMessageObject.TYPE_EXCEL ||
                    item.getType() == RecentMessageObject.TYPE_NOTICE) {
                viewHolder.contentTextView.setText("");
            } else {
                viewHolder.contentTextView.setText(item.getContent());
            }
            Bitmap bitmap = null;
            switch (item.getType()) {
                case RecentMessageObject.TYPE_NOTICE_GROUP:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_msg);
                    break;
                case RecentMessageObject.TYPE_GROUP:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_group);
                    break;
                case RecentMessageObject.TYPE_PERSON:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_ic_contact);
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
            viewHolder.headImage.setImageBitmap(bitmap);
            if (item.getType() == RecentMessageObject.TYPE_PERSON) {
                BitmapUtils.INSTANCE.loadBitmap(viewHolder.headImage,item.getImgPath());
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    void backAction() {
        Intent intent = new Intent(getContainerActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    String fragmentTitle() {
        return "关联信息";
    }

    @Override
    List<Integer> setActionViews() {
        return null;
    }
}
