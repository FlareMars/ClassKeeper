package com.flaremars.classmanagers.uis;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.mainui.NoticeGroupActivity;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.RecentNoticeGroupMsg;
import com.flaremars.classmanagers.views.ContextMenuDialog;
import com.flaremars.classmanagers.views.ContextMenuDialogForNoticeGroup;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoticeGroupsFragment extends BaseFragment {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd HH:mm", Locale.CHINA);

    private List<RecentNoticeGroupMsg> items;

    private MyAdapter adapter;

    public NoticeGroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice_groups, container, false);
        final ListView contentListView = (ListView) view.findViewById(R.id.lv_notice_groups_content);
        final ClassObject curClass = DataSupport.where("classID=?",MainActivity.BASE_GLOBAL_DATA.getCurClassID()).find(ClassObject.class).get(0);

        List<NoticeGroupObject> noticeGroupObjects = DataSupport.where("classobject_id=?", String.valueOf(curClass.getId())).find(NoticeGroupObject.class);

        items = new ArrayList<>();
        for (NoticeGroupObject temp : noticeGroupObjects) {
            List<RecentNoticeGroupMsg> check = DataSupport.where("noticegroupobject_id=?",String.valueOf(temp.getId())).find(RecentNoticeGroupMsg.class,true);
            if (check.size() > 0) {
                RecentNoticeGroupMsg target = check.get(0);
                target.setNoticeGroup(temp);
                items.add(check.get(0));
            }
        }
        adapter = new MyAdapter(getContainerActivity(), items);
        contentListView.setAdapter(adapter);

        contentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecentNoticeGroupMsg recentNoticeGroupMsg = items.get(position);
                recentNoticeGroupMsg.setToDefault("noReadNumber");
                recentNoticeGroupMsg.update(recentNoticeGroupMsg.getId());

                Intent intent = new Intent(getContainerActivity(), NoticeGroupActivity.class);
                intent.putExtra("target_notice_group", recentNoticeGroupMsg.getNoticeGroup().getId());
                intent.putExtra("is_show_input_layout",getContainerActivity().getIntent().getBooleanExtra("is_show_input_layout",false));
                startActivity(intent);

                getContainerActivity().finish();
            }
        });

        contentListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final RecentNoticeGroupMsg item = items.get(position);
                ContextMenuDialogForNoticeGroup.getFunctionDialog(new ContextMenuDialog.OnDeleteClickListener() {
                    @Override
                    public void onDeleteClick() {
                        items.remove(item);
                        DataSupport.delete(RecentNoticeGroupMsg.class, item.getId());
                        adapter.notifyDataSetChanged();
                    }
                }, new ContextMenuDialog.OnUpClickListener() {
                    @Override
                    public void onUpClick() {

                    }
                }, item.getNoticeGroup().getId(), item.getNoticeGroup().getName()).show(getContainerActivity().getSupportFragmentManager(),"tag");
                return true;
            }
        });
        return view;
    }

    private class MyAdapter extends BaseAdapter{

        private List<RecentNoticeGroupMsg> items;

        private Context context;

        public MyAdapter(Context context,List<RecentNoticeGroupMsg> items) {
            this.items = items;
            this.context = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public RecentNoticeGroupMsg getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            public ImageView headImage;

            public TextView nameTextView;

            public TextView contentTextView;

            public TextView noReadNumTextView;

            public TextView timeTextView;

            public FrameLayout noReadNumberBG;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final RecentNoticeGroupMsg item = getItem(position);
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_chatting,null);
                viewHolder = new ViewHolder();
                viewHolder.headImage = (ImageView) convertView.findViewById(R.id.item_chatting_img);
                viewHolder.contentTextView = (TextView) convertView.findViewById(R.id.item_chatting_content);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_chatting_name);
                viewHolder.noReadNumTextView = (TextView) convertView.findViewById(R.id.item_chatting_no_read);
                viewHolder.timeTextView = (TextView) convertView.findViewById(R.id.item_chatting_last_time);
                viewHolder.noReadNumberBG = (FrameLayout) convertView.findViewById(R.id.fl_chatting_no_read_bg);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            viewHolder.timeTextView.setText(DATE_FORMAT.format(item.getTime()));

            if (item.getNoReadNumber() == 0) {
                viewHolder.noReadNumberBG.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.noReadNumberBG.setVisibility(View.VISIBLE);
                viewHolder.noReadNumTextView.setText(item.getNoReadNumber()+"");
            }
            viewHolder.nameTextView.setText(item.getNoticeGroup().getName());

            viewHolder.contentTextView.setText(item.getContent());

            return convertView;
        }
    }
    @Override
    void backAction() {
        Intent intent = new Intent(getContainerActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    String fragmentTitle() {
        return "通知组列表";
    }

    @Override
    List<Integer> setActionViews() {
        return null;
    }
}
