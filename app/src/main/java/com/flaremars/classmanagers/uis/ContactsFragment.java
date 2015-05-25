package com.flaremars.classmanagers.uis;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.ChattingActivity;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.mainui.NoticeGroupActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassMemberObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.DataWrapper;
import com.flaremars.classmanagers.model.FragmentConst;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.RecentNoticeGroupMsg;
import com.flaremars.classmanagers.model.SubClassMemberObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.utils.FlareBitmapUtils;
import com.flaremars.classmanagers.utils.MessagePostUtil;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.ContextMenuDialog;
import com.flaremars.classmanagers.views.ContextMenuDialogForNoticeGroup2;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactsFragment extends BaseFragment {

    private String targetClass;

    private ClassObject targetClassObject;

    private UserObject mineObject;

    private MyAdapter adapter;

    public static ContactsFragment newInstance(String classID) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(AppConst.CLASS_ID, classID);
        fragment.setArguments(args);
        return fragment;
    }

    public ContactsFragment() {
        // Required empty public constructor
//        Bmob.initialize(getContainerActivity(),AppConst.APP_ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetClass = getArguments().getString(AppConst.CLASS_ID);
            targetClassObject = DataSupport.where("classID=?",targetClass).find(ClassObject.class).get(0);
            mineObject = DataSupport.where("userId=?",MainActivity.BASE_GLOBAL_DATA.getUserID()).find(UserObject.class).get(0);
        }
    }

    @Override
    void backAction() {
        if (!getContainerActivity().getSupportFragmentManager().popBackStackImmediate()) {
            Intent intent = new Intent(getContainerActivity(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    String fragmentTitle() {
        return "联系人";
    }

    @Override
    List<Integer> setActionViews() {
        List<Integer> views = new ArrayList<>();
        views.add(R.id.main_container_popup_window);
        return views;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        final ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.fragment_contacts_content);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });

        final List<GroupObject> groups = new ArrayList<>();
        List<SubClassObject> subClassObjectList = DataSupport.where("classobject_id=?",String.valueOf(targetClassObject.getId())).find(SubClassObject.class);
        if (subClassObjectList == null) {
            subClassObjectList = new ArrayList<>();
        }


        List<NoticeGroupObject> noticeGroupObjectList = new ArrayList<>();
        if (NormalUtils.INSTANCE.isCreatorOrManager(targetClassObject)) {
            noticeGroupObjectList.addAll(DataSupport.where("classobject_id=?", String.valueOf(targetClassObject.getId())).find(NoticeGroupObject.class));
        }
        List<UserObject> memberList = new ArrayList<>();
        List<ClassMemberObject> tempList = targetClassObject.getMemberList();
        for (ClassMemberObject temp : tempList) {
            UserObject tempUserObject = DataSupport.where("userID=?",temp.getMemberID()).find(UserObject.class).get(0);
            memberList.add(tempUserObject);
        }

        //填充群组列表
        if (subClassObjectList.size() > 0) {
            GroupObject groupObject = new GroupObject("群组");
            List<ItemObject> itemObjectList = new ArrayList<>();
            for (int i = 0;i < subClassObjectList.size();i++) {
                SubClassObject object = subClassObjectList.get(i);
                itemObjectList.add(new ItemObject("img",object.getSubClassName(),
                        object.getId(),ItemType.CLASS));
            }
            groupObject.setChildItems(itemObjectList);
            groups.add(groupObject);
        }

        //填充通知组列表，可能为空
        GroupObject groupObject = new GroupObject("通知组");
        List<ItemObject> itemObjectList = new ArrayList<>();
        if (noticeGroupObjectList.size() > 0) {
            for (int i = 0;i < noticeGroupObjectList.size();i++) {
                NoticeGroupObject object = noticeGroupObjectList.get(i);
                itemObjectList.add(new ItemObject("img",object.getName(),
                        object.getId(),ItemType.NOTICE_GROUP));
            }
        }
        if (itemObjectList.size() > 0) {
            groupObject.setChildItems(itemObjectList);
            groups.add(groupObject);
        }

        //通知联系人列表
        if (memberList.size() > 0) {
            GroupObject groupObjectContacts = new GroupObject("联系人");
            List<ItemObject> itemObjectListContacts = new ArrayList<>();
            for (int i = 0;i < memberList.size();i++) {
                UserObject object = memberList.get(i);
                itemObjectListContacts.add(new ItemObject(object.getImgId(),object.getUserRealName(),
                        object.getId(),ItemType.CONTACTS));
            }
            groupObjectContacts.setChildItems(itemObjectListContacts);
            groups.add(groupObjectContacts);
        }

        adapter = new MyAdapter(getContainerActivity(),groups);
        listView.setAdapter(adapter);

        for (int i = 0;i < groups.size();i++) {
            listView.expandGroup(i);
        }

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ItemObject item = groups.get(groupPosition).getChildItems().get(childPosition);
                ItemType type = item.getType();
                switch (type) {
                    case NOTICE_GROUP:
                        Intent intent3 = new Intent(getContainerActivity(), NoticeGroupActivity.class);
                        intent3.putExtra("target_notice_group", item.getId());
                        startActivity(intent3);
                        break;
                    case CONTACTS:
                        if (mineObject.getId() == item.getId()) {
                            Intent i = new Intent(getContainerActivity(), ContainerActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_PERSONAL_CENTER);
                            i.putExtras(bundle);
                            startActivity(i);
                        } else {
                            Intent intent = new Intent(getContainerActivity(), ChattingActivity.class);
                            intent.putExtra("target", item.getId());
                            intent.putExtra("is_group", false);
                            startActivity(intent);
                        }
                        break;
                    case CLASS:
                        Intent intent2 = new Intent(getContainerActivity(),ChattingActivity.class);
                        intent2.putExtra("target",item.getId());
                        SubClassObject target = DataSupport.find(SubClassObject.class,item.getId());
                        intent2.putExtra("target_conversation",target.getSubClassID());
                        intent2.putExtra("is_group",true);
                        startActivity(intent2);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //group item 也算一个item
                int parentSize = groups.size();
                int groupSize = groups.get(0).getChildItems().size() + 1; //通知组分割位的前面所有item
                int noticeSize = groups.get(1).getChildItems().size() + 1;//所有通知组的个数加上分割位，但是有可能成为联系人组别

                if (position > groupSize && position < noticeSize + groupSize && parentSize == 3) {
                    //通知组item
                    final ItemObject item = groups.get(1).getChildItems().get(position - groupSize - 1);
                    ContextMenuDialogForNoticeGroup2.getFunctionDialog(new ContextMenuDialog.OnDeleteClickListener() {
                        @Override
                        public void onDeleteClick() {
                            DataSupport.delete(NoticeGroupObject.class, item.getId());

                            //删除通知组最近消息
                            List<RecentMessageObject> temp = DataSupport.where("type = 0 and targetID = ?", String.valueOf(item.getId())).
                                    find(RecentMessageObject.class);
                            if (temp.size() > 0) {
                                temp.get(0).delete();
                            }

                            List<RecentNoticeGroupMsg> temp2 = DataSupport.where("noticegroupobject_id=?", String.valueOf(item.getId())).
                                    find(RecentNoticeGroupMsg.class);
                            if (temp2.size() > 0) {
                                temp2.get(0).delete();
                            }

                            groups.get(1).getChildItems().remove(item);
                            adapter.notifyDataSetChanged();
                        }
                    }, targetClass, item.getId(), item.getName(), 0).show(getContainerActivity().getSupportFragmentManager(), "tag");

                } else if (position != 0 && position < groupSize) {
                    //子群Item
                    final int targetPos = position - 1;
                    final ItemObject itemObject = groups.get(0).getChildItems().get(targetPos);
                    ContextMenuDialogForNoticeGroup2.getFunctionDialog(new ContextMenuDialog.OnDeleteClickListener() {
                        @Override
                        public void onDeleteClick() {
                            //一般情况下在子群组排行第一的都是默认创建的全员组
                            if (targetPos == 0) {
                                Toast.makeText(getContainerActivity(), "全体成员子群不能删除", Toast.LENGTH_LONG).show();
                            } else {
                                groups.get(0).getChildItems().remove(itemObject);
                                List<RecentMessageObject> recentMessageObjects = DataSupport.where("targetID=? and type=2", String.valueOf(itemObject.getId())).
                                        find(RecentMessageObject.class);
                                if (recentMessageObjects.size() > 0) {
                                    DataSupport.delete(RecentMessageObject.class, recentMessageObjects.get(0).getId());
                                }
                                adapter.notifyDataSetChanged();

                                final SubClassObject subClassObject = DataSupport.find(SubClassObject.class, itemObject.getId());

                                //发送通知删除了子群
                                List<SubClassMemberObject> memberObjects = subClassObject.getMemberList();
                                List<String> targetIds = new ArrayList<>();
                                for (SubClassMemberObject temp : memberObjects) {
                                    targetIds.add(temp.getMemberID());
                                }
                                JSONObject msgContent = new JSONObject();
                                try {
                                    msgContent.put(MessageConst.MSG_TYPE, MessageConst.QUIT_GROUP);
                                    msgContent.put(MessageConst.CONTENT_TARGET_ID, subClassObject.getSubClassID());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                DataWrapper wrapper = new DataWrapper(
                                        MainActivity.BASE_GLOBAL_DATA.getUserID(),
                                        targetClassObject.getClassID(),
                                        subClassObject.getSubClassID(),
                                        targetIds,
                                        msgContent
                                );

                                int messageId = MessagePostUtil.INSTANCE.wannaPostMessage(wrapper);
                                Log.e("TAG", "messageId = " + messageId);

                                AVQuery<AVUser> query = AVUser.getQuery();
                                query.getInBackground(MainActivity.BASE_GLOBAL_DATA.getUserID(), new GetCallback<AVUser>() {
                                    @Override
                                    public void done(AVUser avUser, AVException e) {
                                        if (e == null) {
                                            avUser.removeAll("subClassIds", Collections.singletonList(subClassObject.getSubClassID()));
                                            avUser.saveInBackground();
                                        }
                                    }
                                });

                                DataSupport.delete(SubClassObject.class, itemObject.getId());
                            }
                        }
                    }, targetClass, itemObject.getId(), itemObject.getName(), 1).show(getContainerActivity().getSupportFragmentManager(), "tag");
                }

                return true;
            }
        });
        return view;
    }

    private class MyAdapter extends BaseExpandableListAdapter {

        private Context context;

        private List<GroupObject> groupObjects;

        private MyAdapter(Context context, List<GroupObject> groupObjects) {
            this.context = context;
            this.groupObjects = groupObjects;
        }

        @Override
        public int getGroupCount() {
            return groupObjects.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return getGroup(groupPosition).childItems.size();
        }

        @Override
        public GroupObject getGroup(int groupPosition) {
            return groupObjects.get(groupPosition);
        }

        @Override
        public ItemObject getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).getChildItems().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return groupPosition * 1000 + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        private class GroupViewHolder {
            public TextView sectionName;
        }

        private class ChildViewHolder {
            public ImageView itemImg;

            public TextView itemName;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupObject groupObject = getGroup(groupPosition);
            GroupViewHolder groupViewHolder;

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_section,null);
                groupViewHolder = new GroupViewHolder();
                groupViewHolder.sectionName = (TextView) convertView.findViewById(R.id.item_section_content);
                convertView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (GroupViewHolder) convertView.getTag();
            }

            groupViewHolder.sectionName.setText(groupObject.getGroup());
            convertView.setEnabled(false);
            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final ItemObject item = getChild(groupPosition,childPosition);
            ChildViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_contacts,null);
                viewHolder = new ChildViewHolder();
                viewHolder.itemImg = (ImageView) convertView.findViewById(R.id.item_contacts_img);
                viewHolder.itemName = (TextView) convertView.findViewById(R.id.item_contacts_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ChildViewHolder) convertView.getTag();
            }
            ItemType type = item.getType();
            Bitmap bitmap;
            switch (type) {
                case CLASS:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_group);
                    viewHolder.itemImg.setImageBitmap(bitmap);
                    break;
                case NOTICE_GROUP:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_notice_group);
                    viewHolder.itemImg.setImageBitmap(bitmap);
                    break;
                case CONTACTS:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_ic_contact);
                    viewHolder.itemImg.setImageBitmap(bitmap);
                    if (item.getPicPath() != null && !item.getPicPath().equals("")) {
                        FlareBitmapUtils.INSTANCE.loadBitmap(viewHolder.itemImg,item.getPicPath());
                    }
                    break;
                default:
                    break;
            }
            viewHolder.itemName.setText(item.getName());

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }


    }

    private class GroupObject {
        private String group;

        private List<ItemObject> childItems;

        private GroupObject(String group) {
            this.group = group;
        }

        public List<ItemObject> getChildItems() {
            return childItems;
        }

        public void setChildItems(List<ItemObject> childItems) {
            this.childItems = childItems;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }
    }

    private enum ItemType {
        CLASS,

        NOTICE_GROUP,

        CONTACTS
    }

    private class ItemObject {
        private String picPath;

        private String name;

        private int id;

        private ItemType type;

        public ItemObject(String picPath, String name, int id, ItemType type) {
            this.picPath = picPath;
            this.name = name;
            this.id = id;
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public ItemType getType() {

            return type;
        }

        public void setType(ItemType type) {
            this.type = type;
        }

        public String getName() {

            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPicPath() {

            return picPath;
        }

        public void setPicPath(String picPath) {
            this.picPath = picPath;
        }
    }
}
