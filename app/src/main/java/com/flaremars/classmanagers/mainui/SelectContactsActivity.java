package com.flaremars.classmanagers.mainui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.ClassMemberObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.widget.WrapLinearLayout;
import com.flaremars.classmanagers.utils.FlareBitmapUtils;
import com.flaremars.classmanagers.views.Dialog;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SelectContactsActivity extends FragmentActivity {

    private List<ItemObject> items;

    private MyAdapter adapter;

    private Map<String,View> selectedViews;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);

        boolean isContainNoticeGroup = getIntent().getBooleanExtra("is_contain_notice_group",false);

        ListView listView = (ListView) findViewById(R.id.lv_select_contacts_content);
        ExpandableListView noticeGroupListView = (ExpandableListView) findViewById(R.id.elv_select_notice_group_content);

        //如果需要使用到通知组才显示
        if (isContainNoticeGroup) {
            noticeGroupListView.setVisibility(View.VISIBLE);
            findViewById(R.id.tag_divider).setVisibility(View.VISIBLE);
            ClassObject curClass = DataSupport.where("classID=?",MainActivity.BASE_GLOBAL_DATA.getCurClassID()).find(ClassObject.class).get(0);
            final List<NoticeGroupObject> noticeGroupObjectList = DataSupport.where("classobject_id=?",String.valueOf(curClass.getId())).
                    find(NoticeGroupObject.class);
            noticeGroupListView.setAdapter(new NoticeGroupSelectAdapter(this, noticeGroupObjectList));
            noticeGroupListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, final int childPosition, long id) {
                    dialog = new Dialog(SelectContactsActivity.this,"提示","确认将录制任务发送到\n【通知组】\n " + noticeGroupObjectList.get(childPosition).getName());
                    dialog.addCancelButton("取消");
                    dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.putExtra("target_notice_group", noticeGroupObjectList.get(childPosition).getId());
                            intent.putExtra("is_new_notice_group", false);
                            setResult(RESULT_OK, intent);
                            SelectContactsActivity.this.finish();
                        }
                    });
                    dialog.show();

                    return true;
                }
            });
        }

        final WrapLinearLayout linearLayout = (WrapLinearLayout) findViewById(R.id.wll_select_contacts_items);
        selectedViews = new HashMap<>();
        items = new ArrayList<>();
        ClassObject classObject = DataSupport.where("classID=?",MainActivity.BASE_GLOBAL_DATA.getCurClassID()).find(ClassObject.class).get(0);
        items.add(new ItemObject("联系人","id",MyAdapter.ISDIVIDER));

        List<UserObject> members = new ArrayList<>();
        List<ClassMemberObject> tempList = classObject.getMemberList();
        for (ClassMemberObject member : tempList) {
            UserObject userObject = DataSupport.where("userID=?",member.getMemberID()).find(UserObject.class).get(0);
            members.add(userObject);
        }
        for (UserObject item : members) {
            items.add(new ItemObject(item.getUserRealName(),item.getUserId(),item.getImgId()));
        }

        adapter = new MyAdapter(this,items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ItemObject item = items.get(position);
                if (item.isSelected()) {
                    item.setIsSelected(false);
                    linearLayout.removeChildView(selectedViews.get(item.getName()));
                    selectedViews.remove(item.getName());
                    adapter.notifyDataSetChanged();
                } else {
                    item.setIsSelected(true);
                    View childView = View.inflate(SelectContactsActivity.this, R.layout.item_contacts_selected, null);
                    ((TextView) childView.findViewById(R.id.tv_contacts_selected_name)).setText(item.getName());
                    selectedViews.put(item.getName(), childView);
                    linearLayout.addChildView(childView);
                    adapter.notifyDataSetChanged();

                    childView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            item.setIsSelected(false);
                            linearLayout.removeChildView(selectedViews.get(item.getName()));
                            selectedViews.remove(item.getName());
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

        findViewById(R.id.btn_select_contacts_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectContactsActivity.this.finish();
            }
        });

        findViewById(R.id.tv_btn_select_contacts_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ItemObject> selectedItems = new ArrayList<>();
                for (ItemObject item : items) {
                    if (item.isSelected()) {
                        selectedItems.add(item);
                    }
                }

                if (selectedItems.size() > 0) {
                    //返回所选择的目标的ID
                    StringBuilder sb = new StringBuilder();
                    for (ItemObject item : selectedItems) {
                        sb.append(item.getId());
                        sb.append("_");
                    }
                    Intent intent = new Intent();
                    intent.putExtra("is_new_notice_group",true);
                    intent.putExtra("contacts_data",sb.toString());
                    setResult(RESULT_OK,intent);
                } else {
                    setResult(RESULT_CANCELED);
                }
                SelectContactsActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private class NoticeGroupSelectAdapter extends BaseExpandableListAdapter {

        private Context context;

        private List<NoticeGroupObject> noticeGroupObjects;

        private NoticeGroupSelectAdapter(Context context, List<NoticeGroupObject> noticeGroupObjects) {
            this.context = context;
            this.noticeGroupObjects = noticeGroupObjects;
        }

        @Override
        public int getGroupCount() {
            return 1;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return noticeGroupObjects.size();
        }

        @Override
        public String getGroup(int groupPosition) {
            return "通知组";
        }

        @Override
        public NoticeGroupObject getChild(int groupPosition, int childPosition) {
            return noticeGroupObjects.get(childPosition);
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
            GroupViewHolder groupViewHolder;

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_notice_group_divider,null);
                groupViewHolder = new GroupViewHolder();
                groupViewHolder.sectionName = (TextView) convertView.findViewById(R.id.tv_contacts_selected_divider);
                convertView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (GroupViewHolder) convertView.getTag();
            }

            groupViewHolder.sectionName.setText("选择单一通知组");
            convertView.setEnabled(false);
            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final NoticeGroupObject item = getChild(groupPosition,childPosition);
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

            viewHolder.itemImg.setImageResource(R.drawable.default_ic_notice_group);
            viewHolder.itemName.setText(item.getName());

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }


    }

    private class ItemObject {

        private String name;

        private String id;

        private String imgPath;

        private boolean isSelected;

        public ItemObject(String name, String id, String imgPath) {
            this.name = name;
            this.id = id;
            this.imgPath = imgPath;
            this.isSelected = false;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImgPath() {
            return imgPath;
        }

        public void setImgPath(String imgPath) {
            this.imgPath = imgPath;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setIsSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }
    }

    private class MyAdapter extends BaseAdapter {

        private Context context;

        private List<ItemObject> items;

        private static final int ITEM_TYPE_COUNT = 2;

        private static final int ITEM_TYPE_NORMAL = 0;

        private static final int ITEM_TYPE_DIVIDER = 1;

        private static final String ISDIVIDER = "divider";

        public MyAdapter(Context context, List<ItemObject> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getViewTypeCount() {
            return ITEM_TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            ItemObject item = getItem(position);
            if (item.getImgPath().equals(ISDIVIDER)) {
                return ITEM_TYPE_DIVIDER;
            } else {
                return ITEM_TYPE_NORMAL;
            }
        }

        @Override
        public boolean isEnabled(int position) {
            int type = getItemViewType(position);
            return type == ITEM_TYPE_NORMAL;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ItemObject getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class NormalViewHolder {
            public ImageView imgHeaderImageView;

            public TextView nameTextView;

            public ImageView isSelectedImageView;
        }

        private class DividerViewHolder {
            public TextView dividerTextView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemObject item = getItem(position);
            int type = getItemViewType(position);
            NormalViewHolder normalViewHolder = null;
            DividerViewHolder dividerViewHolder = null;

            if (convertView == null) {
                if (type == ITEM_TYPE_DIVIDER) {
                    dividerViewHolder = new DividerViewHolder();
                    convertView = View.inflate(context, R.layout.item_contact_divider, null);
                    dividerViewHolder.dividerTextView = (TextView) convertView.findViewById(R.id.tv_contacts_selected_divider);
                    convertView.setTag(dividerViewHolder);
                } else {
                    normalViewHolder = new NormalViewHolder();
                    convertView = View.inflate(context, R.layout.item_contact_normal, null);
                    normalViewHolder.imgHeaderImageView = (ImageView) convertView.findViewById(R.id.iv_contacts_selected_header);
                    normalViewHolder.isSelectedImageView = (ImageView) convertView.findViewById(R.id.iv_contacts_selected_indicator);
                    normalViewHolder.nameTextView = (TextView) convertView.findViewById(R.id.tv_contact_selected_name);
                    convertView.setTag(normalViewHolder);
                }
            } else {
                if (type == ITEM_TYPE_DIVIDER) {
                    dividerViewHolder = (DividerViewHolder) convertView.getTag();
                } else {
                    normalViewHolder = (NormalViewHolder) convertView.getTag();
                }
            }

            if (type == ITEM_TYPE_DIVIDER) {
                dividerViewHolder.dividerTextView.setText(item.getName());
            } else {
                normalViewHolder.nameTextView.setText(item.getName());
                if (item.getImgPath().equals("")) {
                    normalViewHolder.imgHeaderImageView.setImageResource(R.drawable.default_ic_contact);
                } else {
                    FlareBitmapUtils.INSTANCE.loadBitmap(normalViewHolder.imgHeaderImageView, item.getImgPath());
                }
                if (item.isSelected()) {
                    normalViewHolder.isSelectedImageView.setImageResource(R.drawable.ic_selected_yes_red);
                } else {
                    normalViewHolder.isSelectedImageView.setImageResource(R.drawable.ic_selected_no_red);
                }
            }

            return convertView;
        }
    }

}
