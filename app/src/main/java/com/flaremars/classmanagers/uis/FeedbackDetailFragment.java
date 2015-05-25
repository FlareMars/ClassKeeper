package com.flaremars.classmanagers.uis;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.ChattingActivity;
import com.flaremars.classmanagers.model.ExcelFeedbackObject;
import com.flaremars.classmanagers.model.ExcelTaskObject;
import com.flaremars.classmanagers.model.NoticeFeedbackObject;
import com.flaremars.classmanagers.model.NoticeTaskObject;
import com.flaremars.classmanagers.model.UserObject;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedbackDetailFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    public static final int TYPE_NOTICE = 0;

    public static final int TYPE_INFO_COLLECTION = 1;

    private int type;
    private String title;
    private int targetObject;

    public static FeedbackDetailFragment newInstance(String title,int targetObject,int type) {
        FeedbackDetailFragment fragment = new FeedbackDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1,type);
        args.putString(ARG_PARAM2, title);
        args.putInt(ARG_PARAM3, targetObject);
        fragment.setArguments(args);
        return fragment;
    }

    public FeedbackDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(ARG_PARAM1);
            title = getArguments().getString(ARG_PARAM2);
            targetObject = getArguments().getInt(ARG_PARAM3);
        }
    }

    @Override
    void backAction() {
        getContainerActivity().finish();
    }

    @Override
    String fragmentTitle() {
        return "详细情况";
    }

    @Override
    List<Integer> setActionViews() {
        if (type == TYPE_INFO_COLLECTION) {
            List<Integer> action = new ArrayList<>();
            action.add(R.id.main_container_export_excel);
            return action;
        }
        return null;
    }

    private class ItemObject implements Comparable<ItemObject>{
        private String name;

        private boolean isDone;

        private String phoneNumber;

        public ItemObject(String name, String phoneNumber, boolean isDone) {
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.isDone = isDone;
        }

        public String getName() {
            return name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public boolean isDone() {
            return isDone;
        }

        public void setIsDone(boolean isDone) {
            this.isDone = isDone;
        }

        @Override
        public int compareTo(ItemObject another) {
            if (this.isDone && !another.isDone) {
                return 1;
            }
            if (!this.isDone && another.isDone) {
                return -1;
            }
            return 0;
        }
    }

    private class MyAdapter extends BaseAdapter {

        private List<ItemObject> items;

        private Context context;

        public MyAdapter(Context context, List<ItemObject> items) {
            this.context = context;
            this.items = items;
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

        private class ViewHolder {
            public ImageView imageView;

            public TextView nameTextView;

            public ImageView actionTvBtn;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ItemObject item = getItem(position);
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_detail_contacts,null);
                viewHolder = new ViewHolder();
                viewHolder.actionTvBtn = (ImageView) convertView.findViewById(R.id.iv_btn_item_detail_contacts_action);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.item_detail_contacts_img);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_detail_contacts_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.nameTextView.setText(item.getName());
            //viewHolder.imageView.setImageBitmap();
            viewHolder.actionTvBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!item.isDone()) {
                        Intent phoneCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+item.getPhoneNumber()));
                        getContainerActivity().startActivity(phoneCall);
                    }
                }
            });
            if (item.isDone) {
                viewHolder.actionTvBtn.setImageResource(R.drawable.ic_detail_read);
            } else {
                viewHolder.actionTvBtn.setImageResource(R.drawable.ic_detail_phone);
            }
            return convertView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_collection_detail, container, false);

        ListView listView = (ListView) view.findViewById(R.id.lv_fragment_info_collections_detail_content);
        TextView titleTextView = (TextView) view.findViewById(R.id.tv_fragment_info_collection_detail_title);
        titleTextView.setText(title);

        final List<ItemObject> items = new ArrayList<>();
        if (type == TYPE_INFO_COLLECTION) {
            ExcelTaskObject excelTaskObject = DataSupport.find(ExcelTaskObject.class,targetObject);
            List<ExcelFeedbackObject> feedbackObjects = excelTaskObject.getFeedbackObjectList();
            for (ExcelFeedbackObject temp : feedbackObjects) {
                items.add(new ItemObject(temp.getWhoseName(),temp.getTargetPhone(),!temp.getAnswer().equals("")));
            }

        } else {
            NoticeTaskObject noticeTaskObject = DataSupport.find(NoticeTaskObject.class,targetObject);
            List<NoticeFeedbackObject> noticeFeedbackObjects = noticeTaskObject.getFeedbackObjectList();
            for (NoticeFeedbackObject temp : noticeFeedbackObjects) {
                items.add(new ItemObject(temp.getWhoseName(),temp.getTargetPhone(),temp.isDone()));
            }
        }
        Collections.sort(items);
        listView.setAdapter(new MyAdapter(getContainerActivity(), items));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ItemObject temp = items.get(position);
                UserObject userObject = DataSupport.where("userName=?",temp.getPhoneNumber()).find(UserObject.class).get(0);
                Intent intent = new Intent(getContainerActivity(),ChattingActivity.class);
                intent.putExtra("target",userObject.getId());
                intent.putExtra("is_group",false);
                startActivity(intent);
            }
        });
        return view;
    }


}
